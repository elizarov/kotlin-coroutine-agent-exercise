# Агент и плагин для отладки корутин на языке Kotlin

Тестовое задание для летней практики JetBrains 2017.

## Предварительные знания и инструменты

1. **JVM bytecode** https://docs.oracle.com/javase/specs/jvms/se8/html/index.html <br>
   Можно начать отсюда http://www.javaworld.com/article/2077233/core-java/bytecode-basics.html <br>
   И посмотреть сюда https://www.slideshare.net/CharlesNutter/javaone-2011-jvm-bytecode-for-dummies
   
2. Манипуляция байткодом через библиотеку **ASM** http://asm.ow2.org <br>
   Введение здесь http://asm.ow2.org/doc/tutorial.html
   
3. Написание **Java Agent**-ов https://docs.oracle.com/javase/8/docs/api/java/lang/instrument/package-summary.html <br>
   Пример инструментации кода здесь https://www.captechconsulting.com/blogs/not-so-secret-java-agents---part-1

4. Язык **Kotlin** https://kotlinlang.org <br>
   Очень рекомендуется https://kotlinlang.org/docs/tutorials/koans.html

5. **Корутины** в языке Kotlin https://github.com/Kotlin/kotlin-coroutines/blob/master/kotlin-coroutines-informal.md <br>
   А также библиотека к ним https://github.com/Kotlin/kotlinx.coroutines/blob/master/coroutines-guide.md
      
6. Любить и использовать **IntelliJ IDEA** (Community Edition) https://www.jetbrains.com/idea/ <br>
   Котлин поддерживается "из коробки"

7. Работать с проектом на **Gradle** https://gradle.org <br>
   Работа с Котлином описана здесь https://kotlinlang.org/docs/reference/using-gradle.html
   
## Задание

В этом репозитории содержится проект-затравка (открывать в IntelliJ IDEA):

* [Файл Agent.kt](src/main/kotlin/agent/Agent.kt) содержит минимальное тело Java Agent-а.
* [Файл CoroutineExample.kt](src/test/kotlin/example/CoroutineExample.kt) содержит код тестового приложения.

Сборка и запуск приложения

```sh
gradlew runApp
```

При этом запускается код тестового приложения, которое выводит на консоль:

```text
Agent started.
Started!
Done.
```

Первая строчка выводится внутри `Agent`, а остальные две внутри `CoroutineExample`.

Ваша задача модифицировать `Agent` так, чтобы все подгужаемые классы приложения трансформировались используя ASM. 
При этом, внутри байткода `CoroutineExample` есть вот такой вызов:

```text
INVOKESTATIC example/CoroutineExampleKt.test (Lkotlin/coroutines/experimental/Continuation;)Ljava/lang/Object;
```

> Посмотреть байткод можно в IntelliJ перейдя на строку `test()` в `CoroutineExample` и сделав через 
  Ctrl/Cmd-Shift-A акцию "Show Kotlin Bytecode"
  
В процессе трансформации ваш агент должен искать именно этот вызов (в любом загружаемом коде) и 
вставлять прямо перед ним вывод на экран строки "Test detected". 
В результате, при запуске приложения должно получиться: 

```text
Agent started.
Started!
Test detected
Done.
```

Обратите внимание, что при модификации тестового приложения (добавление более сложной логики, множественные вызовы `test()`) 
транформация должна продолжать перехватывать все вызовы `test()` и помечать их выводом на экран "Test detected".

