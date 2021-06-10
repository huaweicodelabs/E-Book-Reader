# E-Book reader
# Huawei Mobile Services
Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.

## Table of Contents

 * [Introduction](#introduction)
 * [Installation](#installation)
 * [Configuration ](#configuration)
 * [Supported Environments](#supported-environments)
 * [Sample Code](#sample-code) 
 * [License](#license)

## Introduction : E-Book reader
E-Book reader enables users to browse, buy, download and read e-books. The application can narrate the pdf files and  translate the whole book for user.

## Installation
    Install the program on Huawei Android mobile phones.
    
## Supported Environments
    HMS Core (APK) 4.0.0 or later has been installed on Huawei Android phones.
	
## Configuration 
    Create an app in AppGallery Connect and obtain the project configuration file agconnect-services.json. 
    In Android Studio, switch to the Project view and move the agconnect-services.json file to the root directory of the app.
    Change the value of applicationId in the build.gradle file of the app to the name of the app package applied for in the preceding step.
	
## Sample Code
    The E-Book reader program integrates the Huawei ML kit,HMS Core Ads SDK, Huawei Auth UI SDK, Push kit, IAP and app linking.
    The following class in the demo is used for loading and displaying banner ads and loading e-books list:

    MyBooksFragment.java
    Loads and displays banner ads.
    Code location:Education\E_libri\app\src\main\java\com\huawei\elibri\view\fragment\MyBooksFragment.java
    
    For Displaying books, ML TTS and translate function and add favaroiute page:
    
    DisplayBook.java
    Display book, narrate and translate book, night mode function and mark your favourite page of book
    Code location:Education\E_libri\app\src\main\java\com\huawei\elibri\view\fragment\DisplayBook.java
    
    See your favourite pages book wise: 
    
    BookmarkFragment.java
    Code location:Education\E_libri\app\src\main\java\com\huawei\elibri\view\fragment\BookmarkFragment.java
    
    Take membership plan to enjoy ads free service and see more premium books
    PremiumFragment.java
    Code location:Education\E_libri\app\src\main\java\com\huawei\elibri\view\fragment\PremiumFragment.java
    
    Suscribe your interest, according to that user will get push notification for books:
    
    InterestFragment.java
    Code location:Education\E_libri\app\src\main\java\com\huawei\elibri\view\fragment\InterestFragment.java
    
    
## License
    * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
    * Copyright (C) 2017 MINDORKS NEXTGEN PRIVATE LIMITED
    * Software: glide
        Copyright 2014 Google
    * Software: Itext 
        GNU General Lesser Public License (LGPL) version 3.0 - http://www.gnu.org/licenses/lgpl.html
        Mozilla Public License Version 2.0 - http://www.mozilla.org/MPL/2.0/
     
    
    
