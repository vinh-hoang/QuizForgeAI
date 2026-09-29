# Quiz generation: prompt flow

This sequence focuses on the prompts and their purpose. The request supplies a topic, difficulty, and question count (3, 5, or 7).

## Sequence diagram

```mermaid
sequenceDiagram
    actor User
    participant Quiz as Quiz generator
    participant AI as AI model
    participant DB as Database

    User->>Quiz: Request quiz (topic, difficulty, question count)

    Quiz->>AI: Prompt 1: Assess topic viability
    Note over Quiz,AI: Recognizable topic that can support a question?
    AI-->>Quiz: Topic viability result

    alt Topic is blank or not viable
        Quiz-->>User: Reject request, ask for a recognizable topic
    else Topic is viable
        loop For each requested question, in order
            loop Up to 5 attempts, until one question passes
                Quiz->>AI: Prompt 2: Draft one question
                Note over Quiz,AI: 4 choices, 1 answer, a hint. Avoid repeats.
                AI-->>Quiz: Free-text question draft

                Quiz->>AI: Prompt 3: Review and repair
                Note over Quiz,AI: Check facts, clarity, choices, answer, and hint.
                AI-->>Quiz: Validity result and reviewed draft

                alt Review accepts the draft
                    Quiz->>AI: Prompt 4: Structure the reviewed draft
                    Note over Quiz,AI: Preserve content. Fill question, options, and hint.
                    AI-->>Quiz: Structured question fields
                    Quiz->>Quiz: Check required fields and distinct options
                    alt Fields pass validation
                        Quiz->>Quiz: Keep this question, stop retrying it
                    else Fields fail validation
                        Quiz->>Quiz: Retry this question
                    end
                else Review rejects the draft
                    Quiz->>Quiz: Retry this question
                end
            end
            Note over Quiz: Keep the accepted question. If all 5 attempts fail, stop quiz creation.
        end

        alt Every requested question passed
            Quiz->>DB: Save completed quiz and its questions
            DB-->>Quiz: Saved quiz
            Quiz-->>User: Return quiz and its first question
        else A question exhausted its 5 attempts
            Quiz-->>User: Quiz creation fails, nothing is saved
        end
    end
```

If a question-generation call (Prompts 2-4) errors, the review rejects a draft, or the structured fields fail validation, the generator retries that question from Prompt 2, up to five attempts. Questions are generated in order so later draft prompts can avoid earlier questions. The quiz is saved only after every requested question succeeds.

## What each prompt is for

1. **Assess topic viability:** Prevents spending the generation steps on blank, gibberish, or unrecognizable topics. It returns a yes/no decision, not a quiz question.
2. **Draft one question:** Produces a question of about 30 words or fewer for the requested topic and difficulty, with four choices, one intended correct answer, and a hint. It also receives earlier question texts to reduce repetition.
3. **Review and repair:** Checks the draft for factual accuracy, clarity, four distinct choices, one defensible correct answer, and a useful accurate hint. It may fix the draft; if it cannot make a valid question confidently, it rejects it so the generator can retry.
4. **Structure the reviewed draft:** Converts the accepted text into separate `question`, `optionA`-`optionD`, and `hint` fields while preserving its content. It does not produce the answer key; the correct option is determined later when the user answers the question.
