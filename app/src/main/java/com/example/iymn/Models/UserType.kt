package com.example.iymn.Models

sealed class UserType {
    object Admin: UserType()
    object Donor: UserType()
    object Ngo: UserType()

}