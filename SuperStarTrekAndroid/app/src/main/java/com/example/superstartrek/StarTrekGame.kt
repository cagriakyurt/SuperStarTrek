package com.example.superstartrek

import kotlinx.coroutines.channels.Channel
import kotlin.random.Random
import kotlin.math.sqrt

// A partial Kotlin translation of SUPER STARTREK
class StarTrekGame(private val io: IOHandler) {

    private val inputChannel = Channel<String>()

    fun provideInput(input: String) {
        inputChannel.trySend(input)
    }

    private suspend fun input(prompt: String = ""): String {
        if (prompt.isNotEmpty()) {
            io.print(prompt)
        }
        return inputChannel.receive()
    }
    
    // BASIC core variables
    private val G = Array(9) { IntArray(9) }
    private val C = Array(11) { DoubleArray(3) }
    private val K = Array(4) { DoubleArray(4) }
    private val N = IntArray(4)
    private val Z = Array(9) { IntArray(9) }
    private val D = DoubleArray(9)
    
    private var Q1 = 0
    private var Q2 = 0
    private var S1 = 0.0
    private var S2 = 0.0
    private var T = 0.0
    private var T0 = 0.0
    private var T9 = 0.0
    private var E = 3000.0
    private var E0 = 3000.0
    private var P = 10.0
    private var P0 = 10.0
    private var S = 0.0
    private var S9 = 200.0
    private var B9 = 2
    private var K9 = 0
    private var K3 = 0
    private var B3 = 0
    private var S3 = 0
    private var K7 = 0
    private var D0 = 0
    
    private var QStr = ""
    private val emptySector = "   "
    private val devices = arrayOf(
        "", "WARP ENGINES", "SHORT RANGE SENSORS",
        "LONG RANGE SENSORS", "PHASER CONTROL",
        "PHOTON TUBES", "DAMAGE CONTROL",
        "SHIELD CONTROL", "LIBRARY-COMPUTER"
    )
    
    // DEF FNR(R) = INT(RND(R)*7.98+1.01)
    private fun FNR(): Int = (Random.nextDouble() * 7.98 + 1.01).toInt()
    private fun FND(): Double = sqrt((K[1][1]-S1)*(K[1][1]-S1) + (K[1][2]-S2)*(K[1][2]-S2)) // simplified
    
    suspend fun start() {
        while (true) {
            printHeader()
            initializeGame()
            val delayMs = runGameLoop()
            io.println("\nRESTARTING GAME IN ${delayMs / 1000} SECONDS...")
            kotlinx.coroutines.delay(delayMs)
            io.clear()
        }
    }

    private suspend fun printHeader() {
        io.println("                                    ,------*------,")
        io.println("                    ,-------------   '---  ------'")
        io.println("                     '-------- --'      / /")
        io.println("                         ,---' '-------/ /--,")
        io.println("                          '----------------'")
        io.println("                    THE USS ENTERPRISE --- NCC-1701")
        io.println("")
    }
    
    private suspend fun initializeGame() {
        T = (Random.nextInt(20) + 20) * 100.0
        T0 = T
        T9 = 25.0 + Random.nextInt(10)
        
        Q1 = FNR(); Q2 = FNR()
        S1 = FNR().toDouble(); S2 = FNR().toDouble()
        E = E0
        P = P0
        S = 0.0
        K9 = 0
        B9 = 0
        
        // C matrix initialization
        C[1][2] = 1.0; C[2][2] = 1.0; C[6][1] = 1.0; C[7][1] = 1.0
        C[8][1] = 1.0; C[8][2] = 1.0; C[9][2] = 1.0
        C[3][1] = -1.0; C[2][1] = -1.0; C[4][1] = -1.0; C[4][2] = -1.0
        C[5][2] = -1.0; C[6][2] = -1.0
        
        for (i in 1..8) D[i] = 0.0
        
        // Setup Galaxy
        for (i in 1..8) {
            for (j in 1..8) {
                Z[i][j] = 0
                var k_count = 0
                val r1 = Random.nextDouble()
                if (r1 > .98) { k_count = 3; K9 += 3 }
                else if (r1 > .95) { k_count = 2; K9 += 2 }
                else if (r1 > .80) { k_count = 1; K9 += 1 }
                
                var b_count = 0
                if (Random.nextDouble() > .96) { b_count = 1; B9++ }
                G[i][j] = k_count * 100 + b_count * 10 + FNR()
            }
        }
        
        if (K9 > T9) T9 = K9 + 1.0
        
        if (B9 == 0) {
            if (G[Q1][Q2] < 200) { G[Q1][Q2] += 120; K9++ }
            B9 = 1
            G[Q1][Q2] += 10
            Q1 = FNR(); Q2 = FNR()
        }
        
        K7 = K9
        io.println("YOUR ORDERS ARE AS FOLLOWS:")
        io.println("DESTROY THE $K9 KLINGON WARSHIPS")
        io.println("BEFORE THEY ATTACK FEDERATION HQ.")
        io.println("STARDATE: ${T0+T9}. YOU HAVE $T9 DAYS.")
        io.println("THE GALAXY HAS $B9 STARBASES.")
        io.println("")
        
        enterQuadrant()
    }
    
    private suspend fun enterQuadrant() {
        io.println("NOW ENTERING QUADRANT $Q1,$Q2 ...")
        Z[Q1][Q2] = G[Q1][Q2]
        
        K3 = G[Q1][Q2] / 100
        B3 = (G[Q1][Q2] / 10) % 10
        S3 = G[Q1][Q2] % 10
        
        if (K3 > 0) {
            io.println("COMBAT AREA      CONDITION RED")
            if (S < 200) {
                io.println("   SHIELDS DANGEROUSLY LOW")
            }
        }
        
        for (i in 1..3) { K[i][1]=0.0; K[i][2]=0.0; K[i][3]=0.0 }
        
        setupSectorStrings()
        
        // This is a simplified positioning logic
        insertInSector(S1.toInt(), S2.toInt(), "<*>")
        for (i in 1..K3) {
            val (r1, r2) = findEmptySpace()
            insertInSector(r1, r2, "+K+")
            K[i][1] = r1.toDouble(); K[i][2] = r2.toDouble()
            K[i][3] = S9 * (0.5 + Random.nextDouble())
        }
        if (B3 > 0) {
            val (r1, r2) = findEmptySpace()
            insertInSector(r1, r2, ">!<")
        }
        for (i in 1..S3) {
            val (r1, r2) = findEmptySpace()
            insertInSector(r1, r2, " * ")
        }
        
        shortRangeScan()
    }
    
    private fun setupSectorStrings() {
        QStr = ""
        for(i in 1..64) QStr += "   "
    }
    
    private fun findEmptySpace(): Pair<Int, Int> {
        while(true) {
            val r1 = FNR()
            val r2 = FNR()
            val pos = (r1 - 1) * 24 + (r2 - 1) * 3
            if (QStr.substring(pos, pos + 3) == "   ") return Pair(r1, r2)
        }
    }
    
    private fun insertInSector(x: Int, y: Int, obj: String) {
        val pos = (x - 1) * 24 + (y - 1) * 3
        if (pos >= 0 && pos + 3 <= QStr.length) {
            QStr = QStr.substring(0, pos) + obj + QStr.substring(pos + 3)
        }
    }
    
    private suspend fun runGameLoop(): Long {
        var isPlaying = true
        var endDelay = 3000L
        while(isPlaying) {
            if (S <= 0.0 && E <= 0.0) {
                endDelay = 10000L
                isPlaying = false
                break
            }
            if (S + E <= 10.0 && (E <= 10.0 || D[7] == 0.0)) {
                io.println("\n** FATAL ERROR **   YOU'VE JUST STRANDED YOUR SHIP IN SPACE")
                endDelay = 10000L
                isPlaying = false
                break
            }
            
            val cmd = input("COMMAND> ")
            when(cmd.uppercase().trim().take(3)) {
                "NAV" -> navCmd()
                "SRS" -> shortRangeScan()
                "LRS" -> longRangeScan()
                "PHA" -> phaserControl()
                "TOR" -> photonTorpedoes()
                "SHE" -> shieldControl()
                "DAM" -> damageControl()
                "COM" -> libraryComputer()
                "XXX" -> {
                    io.println("RESIGNING COMMAND.")
                    endDelay = 3000L
                    isPlaying = false
                }
                else -> {
                    io.println("ENTER ONE OF THE FOLLOWING:")
                    io.println("  NAV  (TO SET COURSE)")
                    io.println("  SRS  (FOR SHORT RANGE SENSOR SCAN)")
                    io.println("  LRS  (FOR LONG RANGE SENSOR SCAN)")
                    io.println("  PHA  (TO FIRE PHASERS)")
                    io.println("  TOR  (TO FIRE PHOTON TORPEDOES)")
                    io.println("  SHE  (TO RAISE OR LOWER SHIELDS)")
                    io.println("  DAM  (FOR DAMAGE CONTROL REPORTS)")
                    io.println("  COM  (TO CALL ON LIBRARY-COMPUTER)")
                    io.println("  XXX  (TO RESIGN YOUR COMMAND)")
                }
            }
            
            var repairRate = 0.1
            if (D[6] < 0.0) repairRate = 0.05
            for (i in 1..8) {
                if (D[i] < 0.0) {
                    D[i] += repairRate
                    if (D[i] > -0.05 && D[i] < 0.0) D[i] = -0.05
                    if (D[i] >= 0.0) {
                        D[i] = 0.0
                        io.println("DAMAGE CONTROL REPORTS: ${devices[i]} REPAIR COMPLETED")
                    }
                }
            }
            
            if (kotlin.random.Random.nextDouble() <= 0.15) {
                val timeRemaining = T0 + T9 - T
                if (timeRemaining < T9 * 0.3 && timeRemaining > 0) {
                    io.println("LT. UHURA REPORTS:")
                    io.println("  'CAPTAIN, STARFLEET COMMAND URGES EXPEDITION OF MISSION.")
                    io.println("   WE HAVE ${(kotlin.math.floor(timeRemaining*10)/10.0)} STARDATES LEFT.'")
                }
            }
            
            if (T > T0 + T9 || K9 <= 0) {
                if (K9 <= 0) {
                    io.println("CONGRATULATIONS! YOUR MISSION IS COMPLETE.")
                } else {
                    io.println("IT IS STARDATE ${(T*10).toInt() / 10.0}")
                    io.println("THE FEDERATION HAS BEEN CONQUERED")
                }
                endDelay = 10000L
                isPlaying = false
                break
            }
        }
        
        io.println("\nMISSION ENDED.")
        return endDelay
    }
    
    private suspend fun shortRangeScan() {
        var isDocked = false
        for (i in (S1.toInt() - 1)..(S1.toInt() + 1)) {
            for (j in (S2.toInt() - 1)..(S2.toInt() + 1)) {
                if (i in 1..8 && j in 1..8) {
                    val pos = (i - 1) * 24 + (j - 1) * 3
                    if (pos >= 0 && pos + 3 <= QStr.length && QStr.substring(pos, pos + 3) == ">!<") {
                        isDocked = true
                        break
                    }
                }
            }
        }
        D0 = 0
        var condStr = ""
        if (isDocked) {
             condStr = "DOCKED"
             D0 = 1
             E = E0
             P = P0
             for(dIdx in 1..8) D[dIdx] = 0.0
             io.println("SHIELDS DROPPED FOR DOCKING PURPOSES")
             S = 0.0
        } else {
             if (K3 > 0) condStr = "*RED*"
             else if (E < E0 * .1) condStr = "YELLOW"
             else condStr = "GREEN"
        }
        
        if (D[2] < 0.0) {
            io.println("SHORT RANGE SENSORS ARE OUT")
            return
        }

        io.println("-------------------------")
        for (i in 1..8) {
            var rowStr = ""
            for (j in 1..8) {
                val pos = (i - 1) * 24 + (j - 1) * 3
                rowStr += QStr.substring(pos, pos + 3) + ""
            }
            io.println(rowStr)
        }
        io.println("-------------------------")
        io.println("STARDATE:     ${(T*10).toInt() / 10.0}")
        io.println("CONDITION:    $condStr")
        io.println("QUADRANT:     $Q1,$Q2")
        io.println("SECTOR:       ${S1.toInt()},${S2.toInt()}")
        io.println("TORPEDOES:    ${P.toInt()}")
        io.println("ENERGY:       ${E.toInt()}")
        io.println("SHIELDS:      ${S.toInt()}")
        io.println("KLINGONS:     $K9")
        io.println("-------------------------")
    }
    
    // Navigation
    private suspend fun navCmd() {
        if (D[1] < 0.0) {
            io.println("WARP ENGINES DAMAGED. MAXIMUM SPEED = WARP 0.2")
        }
        val courseStr = input("COURSE (1-9): ")
        val warpStr = input("WARP FACTOR (0-8): ")
        
        var c1 = courseStr.toDoubleOrNull()
        if (c1 == null) {
            io.println("   LT. SULU REPORTS, 'INCORRECT COURSE DATA, SIR!'")
            return
        }
        var w = warpStr.toDoubleOrNull()
        if (w == null) {
            io.println("   CHIEF ENGINEER SCOTT REPORTS, 'THE ENGINES WON'T TAKE IT, CAPTAIN!'")
            return
        }
        
        if (w == 0.0) return
        
        if (D[1] < 0.0 && w > 0.2) {
             io.println("WARP ENGINES ARE DAMAGED. EXECUTING WARP 0.2 INSTEAD.")
             w = 0.2
        }
        
        val n = (w * 8 + 0.5).toInt()
        if (E - n < 0) {
            io.println("INSUFFICIENT ENERGY.")
            return
        }
        
        E -= n + 10
        io.println("WARP ENGINES ENGAGED.")
        
        if (c1 == 9.0) c1 = 1.0
        val cInt = c1.toInt()
        val cFrac = c1 - cInt
        val x1 = C[cInt][1] + (C[cInt + 1][1] - C[cInt][1]) * cFrac
        val x2 = C[cInt][2] + (C[cInt + 1][2] - C[cInt][2]) * cFrac
        
        val oldS1 = S1.toInt()
        val oldS2 = S2.toInt()
        
        var x = (Q1 - 1) * 8 + (S1 - 1) + n * x1
        var y = (Q2 - 1) * 8 + (S2 - 1) + n * x2

        var newQ1 = (x / 8.0).toInt() + 1
        var newQ2 = (y / 8.0).toInt() + 1
        
        if (newQ1 < 1 || newQ1 > 8 || newQ2 < 1 || newQ2 > 8) {
             io.println("LT. UHURA REPORTS MESSAGE FROM STARFLEET COMMAND:")
             io.println("  'PERMISSION TO ATTEMPT CROSSING OF GALACTIC PERIMETER")
             io.println("  IS HEREBY *DENIED*. SHUT DOWN YOUR ENGINES.'")
             if (newQ1 < 1) newQ1 = 1
             if (newQ1 > 8) newQ1 = 8
             if (newQ2 < 1) newQ2 = 1
             if (newQ2 > 8) newQ2 = 8
             x = (newQ1 - 1) * 8.0 + 0.0
             y = (newQ2 - 1) * 8.0 + 0.0
        }

        S1 = x % 8.0 + 1.0
        S2 = y % 8.0 + 1.0
        T += 1.0
        
        if (w >= 1.0 && kotlin.random.Random.nextDouble() <= 0.15) {
             val r1 = kotlin.random.Random.nextInt(8) + 1
             D[r1] -= (kotlin.random.Random.nextDouble() * 0.5 + 0.5)
             io.println("DAMAGE CONTROL REPORTS: ${devices[r1]} DAMAGED DURING NAVIGATION")
        }
        
        if (newQ1 != Q1 || newQ2 != Q2) {
             Q1 = newQ1
             Q2 = newQ2
             enterQuadrant()
        } else {
             insertInSector(oldS1, oldS2, "   ")
             insertInSector(S1.toInt(), S2.toInt(), "<*>")
             klingonsShoot()
             shortRangeScan()
        }
    }

    private suspend fun longRangeScan() {
        if (D[3] < 0.0) {
            io.println("LONG RANGE SENSORS ARE INOPERABLE")
            return
        }
        io.println("LONG RANGE SCAN FOR QUADRANT $Q1,$Q2")
        io.println("-------------------")
        for (i in (Q1 - 1)..(Q1 + 1)) {
            var row = ""
            for (j in (Q2 - 1)..(Q2 + 1)) {
                if (i in 1..8 && j in 1..8) {
                    val value = G[i][j]
                    Z[i][j] = value
                    row += ": " + value.toString().padStart(3, '0') + " "
                } else {
                    row += ": *** "
                }
            }
            io.println(row + ":")
            io.println("-------------------")
        }
    }

    private suspend fun shieldControl() {
        if (D[7] < 0.0) {
            io.println("SHIELD CONTROL INOPERABLE")
            return
        }
        io.println("ENERGY AVAILABLE = ${E + S}")
        val inputStr = input("NUMBER OF UNITS TO SHIELDS: ")
        val x = inputStr.toDoubleOrNull()
        if (x == null || x < 0.0 || x == S) {
            io.println("<SHIELDS UNCHANGED>")
            return
        }
        if (x > E + S) {
            io.println("SHIELD CONTROL REPORTS: 'THIS IS NOT THE FEDERATION TREASURY.'")
            io.println("<SHIELDS UNCHANGED>")
            return
        }
        E = E + S - x
        S = x
        io.println("DEFLECTOR CONTROL ROOM REPORT:")
        io.println("  'SHIELDS NOW AT ${S.toInt()} UNITS PER YOUR COMMAND.'")
    }

    private suspend fun phaserControl() {
        if (D[4] < 0.0) {
            io.println("PHASERS INOPERATIVE")
            return
        }
        if (K3 <= 0) {
            io.println("SCIENCE OFFICER SPOCK REPORTS: 'SENSORS SHOW NO ENEMY SHIPS IN THIS QUADRANT'")
            return
        }
        io.println("PHASERS LOCKED ON TARGET; ENERGY AVAILABLE = ${E.toInt()} UNITS")
        val inputStr = input("NUMBER OF UNITS TO FIRE: ")
        val x = inputStr.toDoubleOrNull()
        if (x == null || x < 0.0) {
            io.println("PHASERS UNCHANGED")
            return
        }
        if (x == 0.0) return
        if (E - x < 0) {
            io.println("INSUFFICIENT ENERGY.")
            return
        }
        E -= x
        
        val h1 = (x / K3).toInt()
        for (i in 1..3) {
            if (K[i][3] <= 0.0) continue
            val dist = kotlin.math.sqrt((K[i][1] - S1)*(K[i][1] - S1) + (K[i][2] - S2)*(K[i][2] - S2))
            var distDiv = dist
            if (distDiv < 1.0) distDiv = 1.0
            
            val hit = (h1 / distDiv * (kotlin.random.Random.nextDouble() + 2.0)).toInt()
            
            if (hit > 0.15 * K[i][3]) {
                K[i][3] -= hit.toDouble()
                io.println("$hit UNIT HIT ON KLINGON AT SECTOR ${K[i][1].toInt()},${K[i][2].toInt()}")
                if (K[i][3] <= 0) {
                    io.println("*** KLINGON DESTROYED ***")
                    K3 -= 1
                    K9 -= 1
                    insertInSector(K[i][1].toInt(), K[i][2].toInt(), "   ")
                    K[i][3] = 0.0
                    G[Q1][Q2] -= 100
                    Z[Q1][Q2] = G[Q1][Q2]
                } else {
                    io.println("   (SENSORS SHOW ${K[i][3].toInt()} UNITS REMAINING)")
                }
            } else {
                io.println("SENSORS SHOW NO DAMAGE TO ENEMY AT ${K[i][1].toInt()},${K[i][2].toInt()}")
            }
        }
        klingonsShoot()
    }

    private suspend fun photonTorpedoes() {
        if (D[5] < 0.0) {
            io.println("PHOTON TUBES ARE NOT OPERATIONAL")
            return
        }
        if (P <= 0) {
            io.println("ALL PHOTON TORPEDOES EXPENDED")
            return
        }
        val courseStr = input("PHOTON TORPEDO COURSE (1-9): ")
        var c1 = courseStr.toDoubleOrNull()
        if (c1 == null || c1 < 1.0 || c1 > 9.0) {
            io.println("ENSIGN CHEKOV REPORTS, 'INCORRECT COURSE DATA, SIR!'")
            return
        }
        if (c1 == 9.0) c1 = 1.0
        
        val cInt = c1.toInt()
        val cFrac = c1 - cInt
        val x1 = C[cInt][1] + (C[cInt+1][1] - C[cInt][1]) * cFrac
        val x2 = C[cInt][2] + (C[cInt+1][2] - C[cInt][2]) * cFrac
        
        E -= 2.0
        P -= 1.0
        
        var x = S1
        var y = S2
        io.println("TORPEDO TRACK:")
        
        var trackActive = true
        while(trackActive) {
            x += x1
            y += x2
            val x3 = (x + 0.5).toInt()
            val y3 = (y + 0.5).toInt()
            
            if (x3 < 1 || x3 > 8 || y3 < 1 || y3 > 8) {
                io.println("TORPEDO MISSED")
                trackActive = false
                break
            }
            io.println("               $x3,$y3")
            
            val posStr = QStr.substring((x3 - 1) * 24 + (y3 - 1) * 3, (x3 - 1) * 24 + (y3 - 1) * 3 + 3)
            
            if (posStr == "+K+") {
                io.println("*** KLINGON DESTROYED ***")
                K3 -= 1
                K9 -= 1
                insertInSector(x3, y3, "   ")
                for (i in 1..3) {
                    if (K[i][1].toInt() == x3 && K[i][2].toInt() == y3) {
                        K[i][3] = 0.0
                    }
                }
                G[Q1][Q2] -= 100
                Z[Q1][Q2] = G[Q1][Q2]
                trackActive = false
            } else if (posStr == " * ") {
                io.println("STAR AT $x3,$y3 ABSORBED TORPEDO ENERGY.")
                trackActive = false
            } else if (posStr == ">!<") {
                io.println("*** STARBASE DESTROYED ***")
                B3 -= 1
                B9 -= 1
                insertInSector(x3, y3, "   ")
                G[Q1][Q2] -= 10
                Z[Q1][Q2] = G[Q1][Q2]
                trackActive = false
            }
        }
        klingonsShoot()
    }

    private suspend fun klingonsShoot() {
        if (K3 <= 0) return
        if (D0 != 0) {
            io.println("STARBASE SHIELDS PROTECT THE ENTERPRISE")
            return
        }
        for (i in 1..3) {
            if (K[i][3] <= 0.0) continue
            val dist = kotlin.math.sqrt((K[i][1] - S1)*(K[i][1] - S1) + (K[i][2] - S2)*(K[i][2] - S2))
            var distDiv = dist
            if (distDiv < 1.0) distDiv = 1.0
            
            val hit = (K[i][3] / distDiv * (2.0 + kotlin.random.Random.nextDouble())).toInt()
            S -= hit
            K[i][3] /= (3.0 + kotlin.random.Random.nextDouble())
            
            io.println("$hit UNIT HIT ON ENTERPRISE FROM SECTOR ${K[i][1].toInt()},${K[i][2].toInt()}")
            
            if (S > 0.0) {
                val hitRatio = hit.toDouble() / S
                if (kotlin.random.Random.nextDouble() <= 0.4 && hitRatio > 0.02) {
                    val r1 = kotlin.random.Random.nextInt(8) + 1
                    D[r1] -= hitRatio + 0.5 * kotlin.random.Random.nextDouble()
                    io.println("DAMAGE CONTROL REPORTS: ${devices[r1]} DAMAGED BY HIT")
                }
            }
            
            if (S <= 0.0) {
                io.println("\nTHE ENTERPRISE HAS BEEN DESTROYED. THE FEDERATION")
                io.println("WILL BE CONQUERED.")
                S = 0.0
                E = 0.0
                break
            } else {
                io.println("      <SHIELDS DOWN TO ${S.toInt()} UNITS>")
            }
        }
    }

    private suspend fun damageControl() {
        if (D[6] < 0.0) {
            io.println("DAMAGE CONTROL REPORT NOT AVAILABLE")
            return
        }
        io.println("\nDEVICE            STATE OF REPAIR")
        for (r1 in 1..8) {
            io.println(devices[r1].padEnd(20) + (kotlin.math.floor(D[r1] * 100) / 100.0))
        }
        io.println("")
    }

    private suspend fun libraryComputer() {
        if (D[8] < 0.0) {
            io.println("COMPUTER DISABLED")
            return
        }
        io.println("COMPUTER ACTIVE AND AWAITING COMMAND")
        io.println("  0 = GALACTIC RECORD")
        io.println("  1 = STATUS REPORT")
        io.println("  2 = PHOTON TORPEDO DATA")
        io.println("  3 = STARBASE NAV DATA")
        io.println("  4 = DIRECTION/DISTANCE CALCULATOR")
        
        val aStr = input("COMMAND (0-4): ")
        val a = aStr.toIntOrNull()
        if (a == null || a < 0 || a > 4) {
            io.println("FUNCTIONS AVAILABLE FROM COMPUTER:")
            io.println("   0 = GALACTIC RECORD")
            io.println("   1 = STATUS REPORT")
            io.println("   2 = PHOTON TORPEDO DATA")
            io.println("   3 = STARBASE NAV DATA")
            io.println("   4 = DIRECTION/DISTANCE CALCULATOR")
            return
        }
        
        when (a) {
            0 -> galacticRecord()
            1 -> statusReport()
            2 -> torpedoData()
            3 -> starbaseNavData()
            4 -> distCalc()
        }
    }

    private suspend fun galacticRecord() {
        io.println("\nCOMPUTER RECORD OF GALAXY")
        io.println("    1   2   3   4   5   6   7   8")
        io.println("  -------------------------------")
        for (i in 1..8) {
            var row = "$i "
            for (j in 1..8) {
                if (Z[i][j] == 0) {
                    row += " ***"
                } else {
                    row += " " + Z[i][j].toString().padStart(3, '0')
                }
            }
            io.println(row)
        }
        io.println("")
    }

    private suspend fun statusReport() {
        io.println("   STATUS REPORT:")
        io.println("KLINGON LEFT: ${K9}")
        io.println("MISSION MUST BE COMPLETED IN ${kotlin.math.floor((T0 + T9 - T)*10)/10.0} STARDATES")
        io.println("STARBASES LEFT: ${B9}")
        io.println("TORPEDOES LEFT: ${P.toInt()}")
        io.println("ENERGY RESERVE: ${E.toInt()}")
        io.println("SHIELDS: ${S.toInt()}")
        damageControl()
    }

    private fun calcCourse(targetRow: Double, targetCol: Double): Double {
        val dx = targetCol - S2
        val dy = targetRow - S1
        if (dx == 0.0 && dy == 0.0) return 0.0
        val angle = kotlin.math.atan2(dy, dx)
        var c = 0.0
        if (angle < 0) {
            c = 1.0 - (angle / kotlin.math.PI) * 4.0
        } else {
            c = 1.0 + ((-angle / kotlin.math.PI) * 4.0 + 8.0) % 8.0
            if (c >= 9.0) c = 1.0
            if (angle == kotlin.math.PI) c = 5.0
            if (angle == 0.0) c = 1.0
        }
        return c
    }

    private fun calcDist(targetRow: Double, targetCol: Double): Double {
        val dx = targetCol - S2
        val dy = targetRow - S1
        return kotlin.math.sqrt(dx*dx + dy*dy)
    }

    private suspend fun torpedoData() {
        if (K3 <= 0) {
            io.println("SCIENCE OFFICER SPOCK REPORTS  'SENSORS SHOW NO ENEMY SHIPS")
            io.println("                                IN THIS QUADRANT'")
            return
        }
        var msg = ""
        for (i in 1..3) {
            if (K[i][3] > 0) {
                val c = calcCourse(K[i][1], K[i][2])
                val d = calcDist(K[i][1], K[i][2])
                val cStr = (kotlin.math.floor(c * 100) / 100.0).toString()
                val dStr = (kotlin.math.floor(d * 100) / 100.0).toString()
                msg += "DIRECTION = $cStr\nDISTANCE = $dStr\n"
            }
        }
        io.println(msg.trim())
    }

    private suspend fun starbaseNavData() {
        if (B3 <= 0) {
            io.println("MR. SPOCK REPORTS,  'SENSORS SHOW NO STARBASES IN THIS")
            io.println("                     QUADRANT.'")
            return
        }
        var bx = 0.0
        var by = 0.0
        for (i in 1..8) {
            for (j in 1..8) {
                val pos = (i - 1) * 24 + (j - 1) * 3
                if (QStr.substring(pos, pos + 3) == ">!<") {
                    bx = i.toDouble()
                    by = j.toDouble()
                    break
                }
            }
        }
        val c = calcCourse(bx, by)
        val d = calcDist(bx, by)
        val cStr = (kotlin.math.floor(c * 100) / 100.0).toString()
        val dStr = (kotlin.math.floor(d * 100) / 100.0).toString()
        io.println("DIRECTION = $cStr\nDISTANCE = $dStr")
    }

    private suspend fun distCalc() {
        io.println("DIRECTION/DISTANCE CALCULATOR:")
        io.println("YOU ARE AT QUADRANT $Q1,$Q2 SECTOR ${S1.toInt()},${S2.toInt()}")
        val inputStr = input("PLEASE ENTER INITIAL TARGET COORDINATES (X Y): ")
        val parts = inputStr.trim().split(Regex("[,\\s]+"))
        if (parts.size < 2) return
        val x = parts[0].toDoubleOrNull() ?: return
        val y = parts[1].toDoubleOrNull() ?: return
        
        val inputStr2 = input("PLEASE ENTER FINAL TARGET COORDINATES (X Y): ")
        val parts2 = inputStr2.trim().split(Regex("[,\\s]+"))
        if (parts2.size < 2) return
        val x2 = parts2[0].toDoubleOrNull() ?: return
        val y2 = parts2[1].toDoubleOrNull() ?: return
        
        val dxx = y2 - y 
        val dyy = x2 - x
        if (dxx == 0.0 && dyy == 0.0) {
            io.println("DIRECTION = 0.0")
            io.println("DISTANCE = 0.0")
            return
        }
        val angle = kotlin.math.atan2(dyy, dxx)
        var c = 0.0
        if (angle < 0) {
            c = 1.0 - (angle / kotlin.math.PI) * 4.0
        } else {
            c = 1.0 + ((-angle / kotlin.math.PI) * 4.0 + 8.0) % 8.0
            if (c >= 9.0) c = 1.0
            if (angle == kotlin.math.PI) c = 5.0
            if (angle == 0.0) c = 1.0
        }
        val d = kotlin.math.sqrt(dxx*dxx + dyy*dyy)
        val cStr = (kotlin.math.floor(c * 100) / 100.0).toString()
        val dStr = (kotlin.math.floor(d * 100) / 100.0).toString()
        io.println("DIRECTION = $cStr")
        io.println("DISTANCE = $dStr")
    }
}
