# News Article Search Engine

A Java search engine developed as a team project at Dokuz Eylül University. It indexes news articles using custom hash tables and an inverted index, supporting article ID lookup and ranked text searches.

## Features

- Article lookup by ID
- Free-text search returning the top five matching articles
- Ranking based on query-term frequency
- Text preprocessing with tokenization and stop-word removal
- Custom generic hash tables with dynamic resizing
- Performance comparisons across eight hashing configurations

## Technologies and Concepts

- Java
- Object-Oriented Programming
- Hash Tables
- Inverted Indexing
- Text Preprocessing
- Algorithm Performance Analysis

## Hash Table Configurations

The project compares combinations of:

| Parameter | Options |
|---|---|
| Hash function | Simple Summation Function (SSF), Polynomial Accumulation Function (PAF) |
| Collision handling | Linear Probing, Double Hashing |
| Load factor | 0.5, 0.8 |

Each configuration is evaluated using collision counts, indexing time, and average search time.

## Search Process

1. Load and preprocess the news articles.
2. Build an inverted index mapping words to article IDs and occurrence counts.
3. Preprocess the search query using the same text pipeline.
4. Score matching articles by the frequency of query terms.
5. Return the top five results.

## Dataset

The project report describes experiments using approximately 28,600 CNN news articles.

The full dataset is not included in this repository. Running the application requires a compatible CSV dataset and any auxiliary files referenced by the source code.

Configure the dataset path in the application before running it.

## Running the Project

1. Download or clone the repository.
2. Open the project in a Java IDE, such as IntelliJ IDEA.
3. Configure a Java Development Kit (JDK).
4. Ensure the dataset and required auxiliary files are available at the paths expected by the code.
5. Run the class containing the `main` method.

## Project Report

See [the project report](assignment_report.pdf) for implementation details and experimental results.

## Scope

This is an academic project focused on data structures and search performance. Text ranking uses term frequency rather than semantic search.
