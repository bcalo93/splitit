package com.splitit.domain.usecase

interface UseCase<in P, out R> {
    suspend operator fun invoke(params: P): R
}
