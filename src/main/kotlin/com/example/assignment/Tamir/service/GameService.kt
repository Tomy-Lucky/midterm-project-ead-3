package com.example.assignment.Tamir.service

import com.example.assignment.Tamir.dto.GameDTO
import com.example.assignment.Tamir.dto.GameUpdateOptions
import com.example.assignment.Tamir.dto.PurchasedGame
import com.example.assignment.Tamir.exception.GameAlreadyExistsByNameException
import com.example.assignment.Tamir.exception.GameNotFoundByGameNameException
import com.example.assignment.Tamir.exception.GameNotFoundByIdException
import com.example.assignment.Tamir.model.GameModel
import com.example.assignment.Tamir.repository.GameRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GameService(
    private val gameRepository: GameRepository,
    private val accountService: AccountService
) {

    fun findAll() = gameRepository.findAll().map {
        it.toDTO()
    }

    fun findById(id: Long) = (gameRepository.findByIdOrNull(id) ?: throw GameNotFoundByIdException(id)).toDTO()

    fun findByGameName(gameName: String) =
        (gameRepository.findByGameName(gameName) ?: throw GameNotFoundByGameNameException(gameName)).toDTO()

    @Transactional
    fun addGame(gameDTO: GameDTO): GameDTO {
        if (gameRepository.findByGameName(gameDTO.gameName) != null) throw GameAlreadyExistsByNameException(gameDTO.gameName)

        return gameRepository.save(
            GameModel(
                id = 0,
                gameName = gameDTO.gameName,
                price = gameDTO.price
            )
        ).toDTO()
    }

    @Transactional
    fun modifyGame(id: Long, updateOptions: GameUpdateOptions): GameDTO {
        val gameModel = gameRepository.findByIdOrNull(id) ?: throw GameNotFoundByIdException(id)

        if (updateOptions.gameName != null) gameModel.gameName = updateOptions.gameName
        if (updateOptions.price != null) gameModel.price = updateOptions.price

        return gameModel.toDTO()
    }

    @Transactional
    fun buyGame(cardNumber: String, pinCode: String, gameId: Long): PurchasedGame {
        val game = findById(gameId)

        val account = accountService.withdraw(
            cardNumber = cardNumber,
            pinCode = pinCode,
            amount = game.price
        )

        return PurchasedGame(
            gameName = game.gameName,
            price = game.price,
            message = "You are successfully buy game",
            balance = account.balance
        )
    }

}
