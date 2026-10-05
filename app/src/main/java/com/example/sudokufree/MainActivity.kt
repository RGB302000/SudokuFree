package com.example.sudokufree

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import android.content.Context
import kotlin.random.Random

private const val TEST_BANNER = "ca-app-pub-3940256099942544/6300978111"
private const val TEST_INTERSTITIAL = "ca-app-pub-3940256099942544/1033173712"
private const val TEST_REWARDED = "ca-app-pub-3940256099942544/5224354917"

class MainActivity : ComponentActivity() {
    private var interstitial: InterstitialAd? = null
    private var rewarded: RewardedAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MobileAds.initialize(this) {}
        loadInterstitial()
        loadRewarded()
        setContent { SudokuApp(this) }
    }

    private fun loadInterstitial() {
        InterstitialAd.load(this, TEST_INTERSTITIAL, AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) { interstitial = ad }
                override fun onAdFailedToLoad(error: LoadAdError) { interstitial = null }
            })
    }

    private fun loadRewarded() {
        RewardedAd.load(this, TEST_REWARDED, AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) { rewarded = ad }
                override fun onAdFailedToLoad(error: LoadAdError) { rewarded = null }
            })
    }

    fun showInterstitial() {
        val ad = interstitial ?: return
        ad.show(this)
        interstitial = null
        loadInterstitial()
    }

    fun showRewarded(onReward: () -> Unit) {
        val ad = rewarded
        if (ad == null) { onReward(); loadRewarded(); return }
        ad.show(this) { onReward() }
        rewarded = null
        loadRewarded()
    }
}

@Composable
fun BannerAd(context: Context) {
    AndroidView(factory = {
        AdView(context).apply {
            setAdSize(AdSize.BANNER)
            adUnitId = TEST_BANNER
            loadAd(AdRequest.Builder().build())
        }
    }, modifier = Modifier.fillMaxWidth().height(50.dp))
}

@Composable
fun SudokuApp(activity: MainActivity) {
    val prefs = remember { activity.getSharedPreferences("sudoku", Context.MODE_PRIVATE) }
    var screen by remember { mutableStateOf("home") }
    var difficulty by remember { mutableStateOf("Easy") }
    var puzzle by remember { mutableStateOf(newPuzzle(40)) }
    var selected by remember { mutableStateOf<Pair<Int,Int>?>(null) }
    var hints by remember { mutableIntStateOf(3) }
    var seconds by remember { mutableIntStateOf(0) }
    var won by remember { mutableStateOf(false) }

    val games = prefs.getInt("games",0)
    val wins = prefs.getInt("wins",0)
    val best = prefs.getInt("best_$difficulty",0)

    if (screen == "home") {
        Column(
            Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Sudoku Free", fontSize=36.sp)
            Text("Train your brain", color=Color.Gray)
            Spacer(Modifier.height(28.dp))
            Button(onClick={
                difficulty="Easy"; puzzle=newPuzzle(40); selected=null; hints=3; seconds=0; won=false
                prefs.edit().putInt("games", games+1).apply()
                screen="game"
            }, modifier=Modifier.fillMaxWidth()) { Text("Play Easy") }
            Button(onClick={
                difficulty="Medium"; puzzle=newPuzzle(48); selected=null; hints=3; seconds=0; won=false
                prefs.edit().putInt("games", games+1).apply()
                screen="game"
            }, modifier=Modifier.fillMaxWidth()) { Text("Play Medium") }
            Button(onClick={
                difficulty="Hard"; puzzle=newPuzzle(55); selected=null; hints=3; seconds=0; won=false
                prefs.edit().putInt("games", games+1).apply()
                screen="game"
            }, modifier=Modifier.fillMaxWidth()) { Text("Play Hard") }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick={screen="stats"}, modifier=Modifier.fillMaxWidth()) { Text("Statistics") }
        }
        return
    }

    if (screen == "stats") {
        Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment=Alignment.CenterHorizontally) {
            Text("Statistics", fontSize=30.sp)
            Spacer(Modifier.height(24.dp))
            Text("Games started: $games", fontSize=20.sp)
            Text("Games won: $wins", fontSize=20.sp)
            Text("Win rate: ${if(games==0)0 else wins*100/games}%", fontSize=20.sp)
            Spacer(Modifier.height(18.dp))
            Text("Best Easy: ${formatTime(prefs.getInt("best_Easy",0))}")
            Text("Best Medium: ${formatTime(prefs.getInt("best_Medium",0))}")
            Text("Best Hard: ${formatTime(prefs.getInt("best_Hard",0))}")
            Spacer(Modifier.height(24.dp))
            Button(onClick={screen="home"}) { Text("Back") }
        }
        return
    }

    LaunchedEffect(won) {
        while (!won) { kotlinx.coroutines.delay(1000); seconds++ }
    }

    val board=puzzle.first
    val fixed=puzzle.second

    Column(Modifier.fillMaxSize().padding(12.dp), horizontalAlignment=Alignment.CenterHorizontally) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceBetween, verticalAlignment=Alignment.CenterVertically) {
            Text("←", fontSize=28.sp, modifier=Modifier.clickable{screen="home"})
            Column(horizontalAlignment=Alignment.CenterHorizontally) {
                Text("Sudoku Free", fontSize=25.sp)
                Text(difficulty, color=Color.Gray)
            }
            Text("⏱ ${formatTime(seconds)}")
        }

        Spacer(Modifier.height(8.dp))
        Text("Hints: $hints")
        Spacer(Modifier.height(8.dp))

        for(r in 0..8) Row {
            for(c in 0..8) {
                val v=board[r][c]
                val sel=selected==Pair(r,c)
                Box(
                    Modifier.size(39.dp)
                        .background(if(sel) Color(0xFFDDE7FF) else Color.White)
                        .border(if(r%3==0 || c%3==0) 1.5.dp else .5.dp, Color.DarkGray)
                        .clickable(enabled=!fixed[r][c]){selected=Pair(r,c)},
                    contentAlignment=Alignment.Center
                ) { Text(if(v==0) "" else v.toString(), fontSize=19.sp) }
            }
        }

        Spacer(Modifier.height(8.dp))
        selected?.let { (r,c) ->
            Row(horizontalArrangement=Arrangement.spacedBy(3.dp)) {
                for(n in 1..9) Button(
                    modifier=Modifier.size(37.dp),
                    contentPadding=PaddingValues(0.dp),
                    onClick={
                        if(isValid(board,r,c,n)) {
                            board[r][c]=n
                            if(solved(board)){
                                won=true
                                val newWins=prefs.getInt("wins",0)+1
                                val oldBest=prefs.getInt("best_$difficulty",0)
                                val newBest=if(oldBest==0) seconds else minOf(oldBest,seconds)
                                prefs.edit().putInt("wins",newWins).putInt("best_$difficulty",newBest).apply()
                                activity.showInterstitial()
                            }
                        }
                    }) { Text("$n") }
            }
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick={board[r][c]=0}){Text("Clear")}
                Button(onClick={
                    if(hints>0){ hints--; revealCell(board,fixed) }
                    else activity.showRewarded { hints=1 }
                }){Text(if(hints>0)"Hint" else "Watch Ad +1")}
            }
        }

        if(won) {
            Spacer(Modifier.height(8.dp))
            Text("🎉 Puzzle solved!", fontSize=20.sp)
        }

        Spacer(Modifier.weight(1f))
        BannerAd(activity)
        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick={screen="home"}){Text("Home")}
            Button(onClick={
                val holes=if(difficulty=="Easy")40 else if(difficulty=="Medium")48 else 55
                puzzle=newPuzzle(holes); selected=null; hints=3; seconds=0; won=false
                prefs.edit().putInt("games",prefs.getInt("games",0)+1).apply()
            }){Text("New Game")}
        }
    }
}

fun formatTime(sec:Int):String="${sec/60}:${(sec%60).toString().padStart(2,'0')}"

fun newPuzzle(holes:Int):Pair<Array<IntArray>,Array<BooleanArray>>{
    val b=Array(9){IntArray(9)}; fill(b)
    val f=Array(9){BooleanArray(9){true}}
    var x=0
    while(x<holes){val r=Random.nextInt(9);val c=Random.nextInt(9);if(b[r][c]!=0){b[r][c]=0;f[r][c]=false;x++}}
    return b to f
}
fun fill(b:Array<IntArray>):Boolean{
    for(r in 0..8)for(c in 0..8)if(b[r][c]==0){
        for(n in (1..9).shuffled())if(isValid(b,r,c,n)){b[r][c]=n;if(fill(b))return true;b[r][c]=0}
        return false
    }
    return true
}
fun isValid(b:Array<IntArray>,r:Int,c:Int,n:Int):Boolean{
    for(i in 0..8)if(b[r][i]==n||b[i][c]==n)return false
    val br=r/3*3;val bc=c/3*3
    for(i in br until br+3)for(j in bc until bc+3)if(b[i][j]==n)return false
    return true
}
fun solved(b:Array<IntArray>):Boolean=(0..8).all{r->(0..8).all{c->b[r][c] in 1..9}} &&
    (0..8).all{r->(0..8).all{c->validSelf(b,r,c,b[r][c])}}
fun validSelf(b:Array<IntArray>,r:Int,c:Int,n:Int):Boolean{
    for(i in 0..8)if(i!=c&&b[r][i]==n)return false
    for(i in 0..8)if(i!=r&&b[i][c]==n)return false
    val br=r/3*3;val bc=c/3*3
    for(i in br until br+3)for(j in bc until bc+3)if((i!=r||j!=c)&&b[i][j]==n)return false
    return true
}
fun revealCell(b:Array<IntArray>,f:Array<BooleanArray>){
    val empties=mutableListOf<Pair<Int,Int>>()
    for(r in 0..8)for(c in 0..8)if(!f[r][c]&&b[r][c]==0)empties.add(r to c)
    if(empties.isEmpty())return
    val (r,c)=empties.random()
    for(n in 1..9)if(isValid(b,r,c,n)){b[r][c]=n;return}
}
