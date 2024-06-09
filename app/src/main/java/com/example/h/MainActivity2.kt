package com.example.h

import android.os.Bundle
import android.os.CountDownTimer
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

data class Question(
    val text: String,
    val image: Int, // Resource ID for the image
    val options: List<String>,
    val correctAnswerIndex: Int
)

@Composable
fun MainMenuScreen(onStartQuiz: () -> Unit, onViewLeaderboard: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Black)
    ) {
        Image(
            painter = painterResource(id = R.drawable.menu_background), // Substitua com a imagem de fundo correta
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = onStartQuiz,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(text = "Iniciar Quiz")
            }
            Button(
                onClick = onViewLeaderboard,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(text = "Ver Leaderboard")
            }
        }
    }
}

@Composable
fun LeaderboardScreen(scores: List<Int>, onRestart: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Leaderboard",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        scores.sortedDescending().forEachIndexed { index, score ->
            Text(
                text = "Jogador ${index + 1}: $score pontos",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(4.dp)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRestart,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(text = "Reiniciar Quiz")
        }
    }
}

@Composable
fun QuizApp() {
    var questions by remember { mutableStateOf(sampleQuestions.shuffled()) }
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var score by remember { mutableStateOf(0) }
    var showLeaderboard by remember { mutableStateOf(false) }
    var isCorrectAnswer by remember { mutableStateOf<Boolean?>(null) }
    var scores by remember { mutableStateOf(listOf<Int>()) }
    var playerCounter by remember { mutableStateOf(1) }
    var showMainMenu by remember { mutableStateOf(true) }
    var gameOver by remember { mutableStateOf(false) }
    var timeLeft by remember { mutableStateOf(30) } // Temporizador de 30 segundos
    val scope = rememberCoroutineScope()

    val handleAnswer: (Int) -> Unit = { selectedOptionIndex ->
        if (selectedOptionIndex == questions[currentQuestionIndex].correctAnswerIndex) {
            isCorrectAnswer = true
            score++
        } else {
            isCorrectAnswer = false
        }
        scope.launch {
            delay(1000) // Delay to show color change
            isCorrectAnswer = null
            if (currentQuestionIndex < questions.size - 1) {
                currentQuestionIndex++
                timeLeft = 30 // Reinicia o temporizador para a próxima pergunta
            } else {
                scores = scores + score
                showLeaderboard = true
            }
        }
    }

    val restartQuiz: () -> Unit = {
        questions = sampleQuestions.shuffled()
        currentQuestionIndex = 0
        score = 0
        showLeaderboard = false
        playerCounter++
        showMainMenu = true
        gameOver = false
        timeLeft = 30 // Reinicia o temporizador para o valor inicial
    }

    if (gameOver) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Você perdeu!",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Button(
                onClick = restartQuiz,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(text = "Tentar novamente")
            }
        }
    } else if (showMainMenu) {
        MainMenuScreen(
            onStartQuiz = {
                showMainMenu = false
                showLeaderboard = false
                questions = sampleQuestions.shuffled()
                currentQuestionIndex = 0
                score = 0
                timeLeft = 30 // Inicializa o temporizador para a primeira pergunta
            },
            onViewLeaderboard = {
                showMainMenu = false
                showLeaderboard = true
            }
        )
    } else if (showLeaderboard) {
        LeaderboardScreen(scores = scores, onRestart = restartQuiz)
    } else {
        val currentQuestion = questions[currentQuestionIndex]
        QuestionScreen(
            question = currentQuestion,
            isCorrectAnswer = isCorrectAnswer,
            onOptionSelected = handleAnswer,
            onRestartQuiz = restartQuiz, // Passa a função de callback para reiniciar o quiz
            timeLeft = timeLeft, // Passa o tempo restante para o temporizador
            onTimeExpired = {
                scope.launch {
                    delay(1000)
                    gameOver = true
                }
            }
        )
    }
}



@Composable
fun QuestionScreen(
    question: Question,
    isCorrectAnswer: Boolean?,
    onOptionSelected: (Int) -> Unit,
    onRestartQuiz: () -> Unit,
    timeLeft: Int,
    onTimeExpired: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = when (isCorrectAnswer) {
            true -> Color.Green
            false -> Color.Red
            else -> MaterialTheme.colorScheme.background
        }
    )

    var timeLeft by remember { mutableStateOf(30) } // Temporizador de 30 segundos
    val scope = rememberCoroutineScope()

    // Função para iniciar o temporizador
    fun startTimer() {
        scope.launch {
            repeat(timeLeft) {
                delay(1000)
                timeLeft--
            }
        }
    }

    DisposableEffect(Unit) {
        startTimer() // Inicia o temporizador ao criar o DisposableEffect
        onDispose {
        }
    }

    LaunchedEffect(Unit) {
        delay(timeLeft * 1000L) // Aguarda o término do tempo
        // Atualiza o estado para exibir a tela de "Você perdeu"
    }

    if (timeLeft == 0) {
        // Se o tempo acabou, mostra a tela de "Você perdeu"
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Você perdeu!",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Button(
                onClick = onRestartQuiz, // Chama a função de callback para reiniciar o quiz
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(text = "Tentar novamente")
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .background(color = backgroundColor),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = question.text,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Image(
                painter = painterResource(id = question.image),
                contentDescription = null,
                modifier = Modifier
                    .height(200.dp)
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
            question.options.forEachIndexed { index, option ->
                Button(
                    onClick = { onOptionSelected(index) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                ) {
                    Text(text = option)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Tempo restante: $timeLeft segundos",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                QuizApp()
            }
        }
    }
}
val sampleQuestions = listOf(
    Question(
        text = "Qual é o Pokémon inicial do tipo Fogo na região de Kanto?",
        image = R.drawable.charmander,
        options = listOf("Bulbasaur", "Squirtle", "Charmander", "Pikachu"),
        correctAnswerIndex = 2
    ),
    Question(
        text = "Qual é o tipo do Pokémon Pikachu?",
        image = R.drawable.pikachu,
        options = listOf("Fogo", "Água", "Elétrico", "Terra"),
        correctAnswerIndex = 2
    ),
    Question(
        text = "Qual é o Pokémon Lendário conhecido como 'Deus Pokémon'?",
        image = R.drawable.arceus,
        options = listOf("Mewtwo", "Arceus", "Ratata", "Dialga"),
        correctAnswerIndex = 1
    ),
    Question(
        text = "Qual é o Pokémon conhecido como o 'Pokémon do Tempo'?",
        image = R.drawable.dialga,
        options = listOf("Palkia", "Dialga", "Giratina", "Celebi"),
        correctAnswerIndex = 1
    ),
    Question(
        text = "Qual é o nome da evolução do Eevee do tipo Fogo?",
        image = R.drawable.flareon,
        options = listOf("Vaporeon", "Jolteon", "Flareon", "Espeon"),
        correctAnswerIndex = 2
    ),
    Question(
        text = "Qual é o tipo principal do Pokémon Gengar?",
        image = R.drawable.gengar,
        options = listOf("Fantasma", "Psíquico", "Sombrio", "Venenoso"),
        correctAnswerIndex = 0
    ),
    Question(
        text = "Qual Pokémon é conhecido por ser o rival de Ash Ketchum?",
        image = R.drawable.gary,
        options = listOf("Brock", "Misty", "Gary", "Tracey"),
        correctAnswerIndex = 2
    ),
    Question(
        text = "Qual é o nome da região onde se passa a primeira geração de Pokémon?",
        image = R.drawable.kanto_map,
        options = listOf("Johto", "Hoenn", "Sinnoh", "Kanto"),
        correctAnswerIndex = 3
    ),
    Question(
        text = "Qual é o Pokémon que pode evoluir para Raichu?",
        image = R.drawable.pikachu,
        options = listOf("Eevee", "Pichu", "Pikachu", "Plusle"),
        correctAnswerIndex = 2
    ),
    Question(
        text = "Qual é o nome da organização vilã na região de Kanto?",
        image = R.drawable.team_rocket,
        options = listOf("Equipe Magma", "Equipe Aqua", "Equipe Rocket", "Equipe Galáctica"),
        correctAnswerIndex = 2
    )
)
