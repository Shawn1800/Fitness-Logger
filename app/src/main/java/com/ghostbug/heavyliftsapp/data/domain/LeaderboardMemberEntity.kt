package com.ghostbug.heavyliftsapp.data.domain

import kotlin.time.Instant

data class LeaderboardMemberEntity(
   val  id:String?=null ,
    val leaderboardId:String,
    val userId:String ,
    val status:String,
    val joinedAt: Instant?=null
)
