// package com.github.yjgbg.kepler.dsl
// object build:
//   export core.{*,given}  
//   val project:"project" = compiletime.constValue
//   inline def define(closure:Root >> project.type ?=> Unit):Unit = ???
//   given [A <: _ >> project.type]:NodeKey[project.type,A,project.type] = Key.nodeKey
//   val name: "name" = compiletime.constValue
//   given [A <: _ >> project.type| _ >> task.type]:SingleValueKey[name.type,A,String] = Key.singleValueKey
//   val organization: "organization" = compiletime.constValue
//   given [A <: _ >> project.type]:SingleValueKey[organization.type,A,String] = Key.singleValueKey
//   val task: "task" = compiletime.constValue
//   given [A <: _ >> project.type]:NodeKey[task.type,A,task.type] = Key.nodeKey
//   val action: "action" = compiletime.constValue
//   given [A <: _ >> project.type]:SingleValueKey[action.type,A >> task.type,A => Unit] = Key.singleValueKey
//   val dependsOn: "dependsOn" = compiletime.constValue
//   given [A <: _ >> project.type]:MultiValueKey[dependsOn.type,A,String] = Key.multiValueKey
//   val library: "library" = compiletime.constValue
//   given [A <: _ >> project.type]:MultiValueKey[library.type,A,String] = Key.multiValueKey
//   val repository: "repository" = compiletime.constValue
//   given [A <: _ >> project.type]:MultiValueKey[repository.type,A,String] = Key.multiValueKey
//   val sourceSets: "sourceSets" = compiletime.constValue
//   given [A <: _ >> project.type]:MultiValueKey[sourceSets.type,A,String] = Key.multiValueKey
//   val resourceSets: "resourceSets" = compiletime.constValue
//   given [A <: _ >> project.type]:MultiValueKey[resourceSets.type,A,String] = Key.multiValueKey
// object sample:
//   import build.{*,given}
//   define:
//     repository += "https://"
//     organization := "com.github.yjgbg"
//     sourceSets += "src/main/scala"
//     sourceSets += "src/main/java"
//     resourceSets += "src/main/resources"
//     project(name := "kepler-dsl"):
//       project(name := "kepler-dsl-core")
//       library += "com.github.yjgbg::kepler-dsl:1.0.0"
//       task(name := "printName"):
//         dependsOn += "另一个task的名字"
//         action := { proj =>
//           proj.getSingleValue(name) match
//             case None => println()
//             case Some(value) => println(value)
//         }
//     project(name := "kepler-json-dsl"):
//       dependsOn += "kepler-dsl"
//       task(name := "build"):
//         println()

