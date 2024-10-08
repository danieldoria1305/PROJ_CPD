function on_mult(m_ar::Int, m_br::Int)
    pha = fill(1.0, m_ar * m_ar)
    phb = zeros(Float64, m_br * m_br)

    for i in 1:m_br
        for j in 1:m_br
            phb[(i-1) * m_br + j] = i
        end
    end

    phc = fill(1.0, m_ar * m_ar)

    elapsed_time = @elapsed begin
        for i in 1:m_ar
            for j in 1:m_br
                temp = 0.0
                for k in 1:m_ar
                    temp += pha[(i-1) * m_ar + k] * phb[(k-1) * m_br + j]
                end
                phc[(i-1) * m_ar + j] = temp
            end
        end
    end
    
    println("Time: ", elapsed_time, " seconds")

    println("Result matrix:")
    for i in 1:min(10, m_br)
        print(phc[i], " ")
    end
    println()

    return elapsed_time
end
function on_mult_line(m_ar::Int, m_br::Int)
    pha = fill(1.0, m_ar * m_ar)
    phb = zeros(Float64, m_ar * m_ar)

    for i in 1:m_ar
        for j in 1:m_ar
            phb[(j-1) * m_ar + i] = j
        end
    end

    phc = fill(0.0, m_ar * m_ar)

    elapsed_time = @elapsed begin
        for i in 1:m_ar
            for k in 1:m_ar
                for j in 1:m_br
                    phc[(i-1) * m_ar + j] += pha[(i-1) * m_ar + k] * phb[(k-1) * m_ar + j]
                end
            end
        end
    end
    
    println("Time: ", elapsed_time, " seconds")

    println("Result matrix:")
    for i in 1:min(10, m_br)
        print(phc[i], " ")
    end
    println()

    return elapsed_time
end

function main()
    outputFile = open("resultsMultJulia.csv", "w")
    println(outputFile, "Try,Dimension,Time")

    for trial in 0:10
        # From 600 to 3000, step 400 (OnMult)
        for dim in 600:400:3000
            println("Trial: $trial")
            println("Dimension: $dim")

            elapsed_time = on_mult(dim, dim)  

            # Write results to the CSV file
            println(outputFile, "$trial,$dim,$elapsed_time") 

            println()
        end
    end

    # From 600 to 3000, step 400 (OnMultLine)
    outputFile = open("resultsMultLineJulia.csv", "w")
    println(outputFile, "Try,Dimension,Time")

    for trial in 0:10
        for dim in 600:400:3000
            println("Trial: $trial")
            println("Dimension: $dim")

            elapsed_time = on_mult_line(dim, dim)  

            println(outputFile, "$trial,$dim,$elapsed_time")  

            println()
        end
    end
    close(outputFile)
end

main()
