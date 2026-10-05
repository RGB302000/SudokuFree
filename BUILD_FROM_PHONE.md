# تشغيل اللعبة من الموبايل فقط

## 1) GitHub
- أنشئ حسابًا مجانيًا على GitHub من المتصفح.
- أنشئ Repository جديد باسم `SudokuFree`.
- ارفع كل ملفات المشروع الموجودة في هذا المجلد إلى الـRepository.
- مهم: ارفع مجلد `.github/workflows/build-apk.yml` أيضًا.

## 2) Build
بعد الرفع:
- افتح تبويب **Actions** في الـRepository.
- اختار **Build Sudoku APK**.
- اضغط **Run workflow** لو لم يبدأ تلقائيًا.
- انتظر انتهاء الـBuild.

## 3) تنزيل APK
بعد نجاح الـBuild:
- افتح الـworkflow الناجح.
- انزل إلى **Artifacts**.
- نزّل `SudokuFree-debug-apk`.
- فك الضغط وستجد `app-debug.apk`.
- افتحه على هاتفك وثبته.

## ملاحظة
هذه نسخة Debug للاختبار فقط. بعد التأكد أن اللعبة تعمل، سنعمل Release APK/AAB موقّع للنشر.
