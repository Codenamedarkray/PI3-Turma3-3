package br.edu.puccampinas.superid.functions

import android.util.Log

object validationUtils{
    /**
     * Verifica por meio de regex se o formato do email inserido é valido
     */
    fun emailIsInvalid(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$".toRegex()
        if (!emailRegex.matches(email)) {
            Log.i("FIREBASE", "EMAIL INVALIDO")
            return true
        }
        return false
    }

    /**
     * Verifica se a senha de acesso corresponde as regras pre-definidas
     */
    fun passwordIsInvalid(password: String): Boolean {
        if (password.length < 6) {
            Log.i("FIREBASE", "SENHA INVALIDA")
            return true
        }
        return false
    }
}