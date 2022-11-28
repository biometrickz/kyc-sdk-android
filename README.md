# biometric

## Инструкция для установки

Откройте терминал снизу в Android Studio.

Пропишите команду git clone и url этого репозитория

В корневом файле проекта найдите файл setting.gradle

Добавьте туда эту строку -> include ':biometric'

После, в папке app есть файл build.gradle, там внутри dependencies добавьте эту строку -> implementation project(':biometric')

Теперь нажмите кнопку Sync Project в правом верхнем углу

Внутри файла BiometricDialog вы можете по необходимости изменять данные ссылок:
```
BASE_URL - это основная ссылка
```

## Инструкция для работы
```
BiometricDialog.show(token, supportFragmentManager, this, object : OnUrlChangeListener {
            override fun onResultSuccess(result: String) {
                // Если все успешно
            }

            override fun onResultFailure(reason: String) {
                // Если ошибка
            }
        })
```
