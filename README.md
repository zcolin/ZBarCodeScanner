# BarcodeScannerLib
##二维码扫描库.

####基于 zxing 的二维码扫描库，封装了View视图

## Gradle
app的build.gradle中添加
```
dependencies {
    compile 'com.github.zcolin:ZBarcodeScanner:1.0.5'
}
```
工程的build.gradle中添加
```
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```
