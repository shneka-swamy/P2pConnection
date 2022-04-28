package com.example.p2pconnection

class Constants {
    companion object{
        private val it = 0
        val port =  IntArray(10) { 5000 + (it+1)}
    }
}