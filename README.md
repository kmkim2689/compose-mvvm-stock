## Stock Market Application with Clean Architecture

### Reference
* How to Build a Clean Architecture Stock Market App 📈 (Jetpack Compose, Caching, SOLID) by Philipp Lackner
  * https://youtu.be/uLs2FxFSWU4

### Technologies
* Clean Architecture(SOLID)
* Jetpack Compose
* Dagger-Hilt - Dependency Injection
* Retrofit - Remote API
* Room - Local Caching
* OpenCSV - CSV parsing(Table Format) ***
  * mainly used in stock market
  * especially to render a graph

### Functions of the Application
* **Initial**
  * Top : Search Bar
  * The List of Stock Info Item
    * stock name and symbol
    * market name

* **Search**
  * By every text change, the result appears on the screen
    * the data is cached on the local database

* **Item**
  * when clicked, the detail information about the specific company appears on screen
    * Corp name
    * Symbol
    * industry
    * country
    * description
    * market summary
      * the graph of stock price changes
   
### API
* Alpha Vantage
  * https://alphavantage.co > Get Free Api Key > Fill the form > get the api key
* documentation : https://www.alphavantage.co/documentation/
 
### Dependencies
* build.gradle(project)

      buildscript {
        ext {
          compose_version = '1.1.0'
        }
        dependencies {
          classpath "com.google.dagger:hilt-android-gradle-plugin:2.40.5"
        }
      }
      // Top-level build file where you can add configuration options common to all sub-projects/modules.
      plugins {
          id 'com.android.application' version '7.1.0' apply false
          id 'com.android.library' version '7.1.0' apply false
          id 'org.jetbrains.kotlin.android' version '1.6.10' apply false
      }
      
      task clean(type: Delete) {
          delete rootProject.buildDir
      }

* build.gradle(app)

      plugins {
          id 'com.android.application'
          id 'org.jetbrains.kotlin.android'
          id 'kotlin-kapt'
          id 'dagger.hilt.android.plugin'
          id 'kotlin-parcelize'
          id 'com.google.devtools.ksp' version '1.6.10-1.0.2'
      }
      
      kotlin {
          sourceSets {
              debug {
                  kotlin.srcDir("build/generated/ksp/debug/kotlin")
              }
              release {
                  kotlin.srcDir("build/generated/ksp/release/kotlin")
              }
          }
      }
      // ...
      compileOptions {
          sourceCompatibility JavaVersion.VERSION_17
          targetCompatibility JavaVersion.VERSION_17
      }
      kotlinOptions {
          jvmTarget = '17'
      }

      // ...
      
      dependencies {
      
      
          // OpenCSV
          implementation 'com.opencsv:opencsv:5.5.2'
          
          // Compose dependencies
          implementation "androidx.lifecycle:lifecycle-viewmodel-compose:2.4.1"
          implementation "androidx.compose.material:material-icons-extended:$compose_version"
          implementation "com.google.accompanist:accompanist-flowlayout:0.17.0"
          implementation 'androidx.paging:paging-compose:1.0.0-alpha14'
          implementation "androidx.activity:activity-compose:1.6.0-alpha01"
          implementation "com.google.accompanist:accompanist-swiperefresh:0.24.2-alpha"
          
          // Compose Nav Destinations
          implementation 'io.github.raamcosta.compose-destinations:core:1.1.2-beta'
          ksp 'io.github.raamcosta.compose-destinations:ksp:1.1.2-beta'
          
          // Coil
          implementation "io.coil-kt:coil-compose:1.4.0"
          
          //Dagger - Hilt
          implementation "com.google.dagger:hilt-android:2.40.5"
          kapt "com.google.dagger:hilt-android-compiler:2.40.5"
          implementation "androidx.hilt:hilt-lifecycle-viewmodel:1.0.0-alpha03"
          kapt "androidx.hilt:hilt-compiler:1.0.0"
          implementation 'androidx.hilt:hilt-navigation-compose:1.0.0'
          
          // Retrofit
          implementation 'com.squareup.retrofit2:retrofit:2.9.0'
          implementation 'com.squareup.retrofit2:converter-moshi:2.9.0'
          implementation "com.squareup.okhttp3:okhttp:5.0.0-alpha.3"
          implementation "com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.3"
          
          // Room
          implementation "androidx.room:room-runtime:2.4.2"
          kapt "androidx.room:room-compiler:2.4.2"
          
          // Kotlin Extensions and Coroutines support for Room
          implementation "androidx.room:room-ktx:2.4.2"
      }

### Package Structure
* data / module
  * the actual 'data' related to logic + concrete implementations about 'how to get the data'
  * Retrofit, Room, CSV Parsing everything(every parsing) that is related to data...
* domain
  * contain actual business rules
  * how to filter specific entries
  * actual models that is mapped from dto objects
* presentation
  * show something to user
  * ui, state, viewmodels...

---

* data
  * csv
  * local
  * mapper
  * remote
    * dto : objects that 'directly' represent the json data from the api
  * repository

* domain
  * model
  * repository

* presentation
  * company_listings
  * company_info

* util

---

### Implementing Api and Database

* to implement company list

  * data > remote > StockApi.kt(Interface)
    * Api Interface with Retrofit - to get the list of stock


  * set up local database(-> to put downloaded csv file in database)
    * entity : data > local > CompanyListingEntity.kt(Data Class)
      * set the fields according to the csv file
      * symbol, name, exchange, assetType, ipoDate, delistingDate, status

  * map objects(dto) to domain level models
    * what we want to avoid : polluting presentation layer with data related logic
    * domain > model > CompanyListingModel.kt
    * domain level files have nothing to do with any type of third party library
      * room, retrofit... => no related!!!!!
    * even if data level changes, domain files doesn't have to be changed
      * independent of third-party libraries
  
  * mappers (data level to domain level, and vice versa)
    * mapper > CompanyMapper.kt(File)
    * to define how to take a company listing entity(data layer) and transform it into a company listing model(domain)
    * and vice versa
    
  * define dao of database
    * data > local > StockDao.kt(Interface)
    
  * implement the database
    * data > local > StockDatabase.kt(Class)

### Implementing the repositories
* Interface that represents the functions the repository needs to implement

* domain > repository vs data > repository
  * why?
    * the same reason to have models in data and domain layer
    * viewModel class should only be able to access classes from the domain layer

* domain > repository > StockRepository.kt(Interface)
  * presentation layer is allowed to access...(as it is the domain layer)
  * before implementing function in repository, create the utility class
  
* util > Resource.kt(Sealed Class)
  * to handle data derived from repository when dealing with remote data
  * management of status of data derivation
    * success
    * wrong -> catch the error -> show it in the ui
  * the class is used to distinguish between success and error cases
    * the parameters, data and message are all nullable
    * reason : the case is either success(data) or error(message) or loading(null)

* to come back... StockRepository.kt