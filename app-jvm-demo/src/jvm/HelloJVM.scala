package jvm;

object HelloJVM:
  @main def main = println(s"Hello from ${aa.Platform.current},now is ${aa.Platform.current.timestamp}")