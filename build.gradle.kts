// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.openapi.generator)
}

openApiGenerate {
    // replace remoteInputSpec with inputSpec
    // if the api.yaml file is not remotely hosted
    remoteInputSpec = "https://music.witelokk.ru/api.json"
    // The name of the generator which will handle codegen.
    generatorName = "kotlin"
    // The output target directory into which code will be generated.
    outputDir = "$rootDir/app"
//    // Sets specified global properties.
//    globalProperties.set(mapOf(
//        "browserClient" to 'false',
//        "hideGenerationTimestamp" to 'true'
//    })
    // Defines whether or not model-related test files should be generated.
    generateModelTests = false
    // Defines whether or not api-related test files should be generated.
    generateApiTests = false
    // Defines whether or not model-related documentation files should be generated.
    generateModelDocumentation = false
    // Defines whether or not api-related documentation files should be generated.
    generateApiDocumentation = false
    // A map of options specific to a generator.
    // see https://github.com/OpenAPITools/openapi-generator/blob/master/docs/generators/dart-dio.md#config-options
    // for the full set of available options
    configOptions = mapOf(
        "library" to "jvm-ktor",
        "serializationLibrary" to "kotlinx_serialization",
        "packageName" to "com.witelokk.musicapp.api"
    )
    // see https://github.com/OpenAPITools/openapi-generator/blob/master/modules/openapi-generator-gradle-plugin/README.adoc#openapigenerate
    // for the full set of available options
}
