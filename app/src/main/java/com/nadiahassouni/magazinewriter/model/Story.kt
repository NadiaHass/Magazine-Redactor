package com.nadiahassouni.magazinewriter.model

import java.io.Serializable

class Story (
    var id : String = "" ,
    var imageUrl : String = "" ,
    var title : String = "",
    var date : String = "" ,
    var state : String = ""
    ) : Serializable
