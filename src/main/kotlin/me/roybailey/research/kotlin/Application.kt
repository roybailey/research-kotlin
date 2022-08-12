package me.roybailey.research.kotlin


fun main(args: Array<String>) {

    val envVariableEnabler = "GENERATOR_ENABLED"
    val enabled = System.getenv(envVariableEnabler)

    if ("true" == enabled) {
        println("       _____________   ____________  ___  __________  ____")
        println("      / ____/ ____/ | / / ____/ __ \\/   |/_  __/ __ \\/ __ \\")
        println("     / / __/ __/ /  |/ / __/ / /_/ / /| | / / / / / / /_/ /")
        println("    / /_/ / /___/ /|  / /___/ _, _/ ___ |/ / / /_/ / _, _/")
        println("    \\____/_____/_/ |_/_____/_/ |_/_/  |_/_/  \\____/_/ |_|")
        println("    CODE GENERATOR ENABLED")
        println("")

        // runApplication<GeneratorApplication>(*args)
    } else {
        println("       ____  ____________")
        println("      / __ \\/ ____/ ____/")
        println("     / / / / /_  / /_")
        println("    / /_/ / __/ / __/")
        println("    \\____/_/   /_/")
        println("    CODE GENERATOR DISABLED")
        println("")
        println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
        println("!!!!!!!!!! Generator exiting as $envVariableEnabler != 'true' but == '$enabled'")
        println("!!!!!!!!!! this is normal for CI/CD builds")
        println("!!!!!!!!!! in development you might want to generate code for local builds by assigning $envVariableEnabler=true")
        println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
    }

}

