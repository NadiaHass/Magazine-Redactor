package com.nadiahassouni.magazinewriter.model

import java.io.Serializable

class Article (
    var id : String = "" ,
    var title : String = "" ,
    var imageUrl : String = "" ,
    var text : String = "",
    var category : String = "" ,
    var date : String = "" ,
    var state : String = "",
    var type : String = ""
        ) : Serializable