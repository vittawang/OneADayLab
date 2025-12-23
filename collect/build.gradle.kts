import com.android.build.api.dsl.ProductFlavor

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.sunspot.collect"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = libs.versions.jvmTarget.get().toString()
    }

    val configList = listOf("bottomDialog", "hhh")
    val renamedFlavorList = renameFlavorList(configList)
    //定义多个变种（flavor：buildType）(flavor:bottom)
    setFlavorDimensions(renamedFlavorList)
    //为每个变种创建一种维度（可以创建多种维度 这个pins的场景创建一种维度即可，因为要组合多个flavor，维度不可组合只能选一）（dimension：debug、release、publish）
    renamedFlavorList.forEach {
        android.productFlavors.create(it, object : Action<ProductFlavor> {
            override fun execute(t: ProductFlavor) {
                //(dimension:也叫bottom) 这里纯好记了
                t.dimension = it
            }
        })
    }
    //声明每个维度的具体文件夹路径配置,并组合各个维度变种形成apk
    productFlavors.forEachIndexed { index, flavor ->
        val flavorName = configList[index]
        //sourceSet要以abc命令，具体的文件夹路径用明文
        sourceSets.named(flavor.name) {
            //java.srcDir("src/$flavorName/kotlin", getOtherPath(flavorName, appName, "java")) 这里也可以加other
            java.srcDir("src/$flavorName/java")
            res.srcDir("src/$flavorName/res")
            jniLibs.srcDirs("src/$flavorName/libs")
            assets.srcDir("src/$flavorName/assets")
            manifest.srcFile("src/$flavorName/AndroidManifest.xml")
        }
        flavor.consumerProguardFiles("src/$flavorName/consumer-rules.pro")
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

fun renameFlavorList(list: List<String>): List<String> {
    return list.mapIndexed { index, _ ->
        generateName(index)
    }
}

/**
 * 重命名flavorName 名字是个字符串不能太长
 */
fun generateName(index: Int): String {
    // ASCII码中，'a'的值为97
    val baseChar = 'a'
    val alphabetLength = 26

    // 计算当前索引对应的字母组合
    if (index < alphabetLength) {
        // 单字母命名 abcd.....
        return "${(baseChar + index).toChar()}"
    } else {
        // 双字母命名 AaAbAc....
        val firstCharIndex = (index / alphabetLength) - 1
        val secondCharIndex = index % alphabetLength
        return "${(baseChar + firstCharIndex).toChar()}${(baseChar + secondCharIndex).toChar()}"
    }
}