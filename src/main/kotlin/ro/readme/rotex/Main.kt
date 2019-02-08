package ro.readme.rotex

fun main(args: Array<String>) {
     if (args.isNotEmpty()) {
          ConfigProperties.initialize(args[0])
     }
     BuildController().run()
}
