package com.github.kright.editor

import com.github.kright.interpreter.Output
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread


class InterpreterWrapper {
    private val isFinished = AtomicBoolean(false)

    fun stop() {
        isFinished.set(true)
    }

    val wasStopped: Boolean
        get() = isFinished.get()

    companion object {
        fun runFile(
            interpreterPath: File,
            code: File,
            output: Output,
            stopCallback: (InterpreterWrapper) -> Unit
        ): InterpreterWrapper {
            val result = InterpreterWrapper()

            thread {
                val processBuilder = ProcessBuilder(interpreterPath.absolutePath, code.absolutePath)
                val process = processBuilder.start()
                output.error("run ${code.path}")

                val inputStream = BufferedReader(InputStreamReader(process.inputStream))
                val errorStream = BufferedReader(InputStreamReader(process.errorStream))

                while (true) {
                    if (result.wasStopped) {
                        process.destroyForcibly()
                        output.error("process was terminated")
                        break
                    }
                    if (errorStream.ready()) {
                        output.error(errorStream.readLine())
                        continue
                    }
                    if (inputStream.ready()) {
                        output.print(inputStream.readLine())
                        continue
                    }
                    if (!process.isAlive) {
                        result.stop()
                        output.error("process finished with code ${process.exitValue()}")
                        break
                    }
                    Thread.sleep(1)
                }
                stopCallback(result)
            }

            return result
        }
    }
}
