
A very simple code editor and interpreter

## Interpreter

* REPL mode when running without arguments.
* run code from file specified in first argument.

```./gradlew interpreter:run```

IO may work incorrect because of gradle. 
It's better to build dist ```./gradlew interpreter:distZip``` and run it manually. 

## Editor

```./gradlew editor:run```

Actually, it just loads/saves code and runs it in a separate process.
