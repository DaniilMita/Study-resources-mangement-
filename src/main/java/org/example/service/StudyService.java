package org.example.service;

import org.example.ai.OpenRouterClient;
import org.example.model.LearningProfile;
import org.example.util.Validators;

public final class StudyService {

    private static final String DEFAULT_MODEL =
            System.getenv().getOrDefault("OPENROUTER_MODEL", "openai/gpt-4o-mini");

    /** clientul care trimite request-urile http catre openrouter. */
    private final OpenRouterClient ai = new OpenRouterClient();

    /** modelul folosit de acest serviciu (mereu default_model in aceasta varianta). */
    private final String model = DEFAULT_MODEL;


    public String generateStudyPlan(LearningProfile p) {
        Validators.require(p != null, "Profilul nu poate fi nul.");
        String system = """
                Esti un mentor educational bazat pe dovezi.
                Creezi planuri realiste si structurate.
                Raspunzi in romana.
                """;

        String userPrompt = """
            Creeaza un plan de invatare pe 7 zile pentru urmatorul student:

            Materie principala: %s
            Nivel: %s
            Obiectiv: %s
            Timp zilnic disponibil: %d minute

            Include:
            - obiective zilnice
            - tehnici de invatare adaptate nivelului
            - pauze recomandate
            - recapitulare finala
            """.formatted(p.mainSubject(), p.level(), p.goal(), p.dailyMinutes());

        return ai.chat(model, system, userPrompt, 0.2, 900);
    }

    public String generateRevisionPlan(String subject, int daysUntilExam) {
        Validators.require(daysUntilExam > 0, "Numarul de zile nu poate fi negativ.");
        Validators.require(subject != null && !subject.isBlank(), "Materia nu poate fi vida.");

        String system = "Esti un planificator academic";

        String userPrompt = """
            Creeaza un plan structurat de recapitulare pentru %s.
            Zile ramase pana la examen: %d.
            Raspuns in limba romana.
            """.formatted(subject, daysUntilExam);

        return ai.chat(model,system,userPrompt, 0.6, 400);
    }




    public String generateQuiz(String subject, String difficulty, int questions) {
        Validators.require(subject != null && !subject.isBlank(), "Materia nu poate fi vida.");
        Validators.require(difficulty != null && !difficulty.isBlank(), "Nivelul nu poate fi vide.");
        Validators.require(questions > 0, "Numarul de intrebari nu poate fi negativ.");

        String system = "Esti un academic quiz generator.";

        String userPrompt = """
                Genereaza un test cu %d intrebari despre %s.
                Nivel de dificultate: %s.
                
                Toate intrebarile si raspunsurile trebuie sa fie in limba romana.
                
                Format exact:
                Intrebarea 1:
                A)
                B)
                C)
                D)
                Raspuns corect:
                """.formatted(questions, subject, difficulty);
        return ai.chat(model, system, userPrompt, 0.4, 400);
   }
}



