# 아산병원 프로젝트 (웨어러블을 이용한 실시간 환자 상태 모니터링 서비스)

[comment]: <> ([![Hits]&#40;https://hits.seeyoufarm.com/api/count/incr/badge.svg?url=https%3A%2F%2Fgithub.com%2Fkor45cw%2FRetrofit-tutorial&#41;]&#40;https://hits.seeyoufarm.com&#41;)

스마트워치를 통해 위험 환자로부터 실시간 생체 및 활동 신호를 받아오는 Wear OS 어플리케이션

- Java based
- use Android X
- use databinding

```groovy
android {
    compileSdk 32

    defaultConfig {
        applicationId "com.asanwatch.measure"
        minSdk 28
        targetSdk 32
        versionCode 1
        versionName "1.0"

    }
}
```

## Used Libaray (사용한 라이브러리)

- [Retrofit](https://square.github.io/retrofit)
- [JSONPlaceholder](http://jsonplaceholder.typicode.com)

```groovy
dependencies {
    implementation 'com.squareup.retrofit2:retrofit:2.6.0'
    implementation 'com.squareup.retrofit2:converter-gson:2.6.0'
    implementation 'com.google.android.gms:play-services-wearable:17.1.0'
    implementation 'androidx.percentlayout:percentlayout:1.0.0'
    implementation 'androidx.legacy:legacy-support-v4:1.0.0'
    implementation 'androidx.recyclerview:recyclerview:1.2.1'
    implementation 'androidx.wear:wear:1.1.0'
}
```

## Detail

1. MainActivity : UI 조작 함수 제공
2. Device : 디바이스에 관한 정보들을 가져옵니다. (디바이스 브랜드, 고유 ID, 센서 정보 등)
3. Retrofit : 서버와 http통신을 담당합니다. (api 인터페이스, 데이터 처리 등)
4. Service : 신호를 측정하는 서비스입니다. (fore-ground에서 작동)
5. Setting : 어플리케이션에서 공유하는 오브젝트를 설정 및 제공합니다.

NOTE

- Add Internet permission in AndroidManifest.xml
- Add Wake_lock permission in AndroidManifest.xml
- Add Body-sensors permission in AndroidManifest.xml
- Add Activity-recognition permission in AndroidManifest.xml
- Add Foreground-service in AndroidManifest.xml
- Add Request-ignore-battery-optimization permission in AndroidManifest.xml
- Set to allow using this application in background at paired smartphone's wear-os app (Avoid doze mode)
```
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
    <uses-permission android:name="android.permission.WAKE_LOCK" />
    <uses-permission android:name="android.permission.BODY_SENSORS" />
    <uses-permission android:name="android.permission.ACTIVITY_RECOGNITION" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS"/>
```

## Output
