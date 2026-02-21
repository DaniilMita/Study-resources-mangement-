package org.example.model;

public record StudySession (
      long id,
      long userId,
      String date,
      Integer minutesStudied,
      String topic,
      Integer difficulty,
      String notes
){}
