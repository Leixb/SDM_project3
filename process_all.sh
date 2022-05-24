#!/usr/bin/env bash

NIX="nix run .#jar --"
RUNNER="${RUNNER:-$NIX}"

$RUNNER \
    --node=Paper:Poster=./data/papers_a.csv \
    --node=Paper:DemoPaper=./data/papers_b.csv \
    --node=Paper:FullPaper=./data/papers_c.csv \
    --node=Paper:ShortPaper=./data/papers_d.csv \
    --node=Author=./data/authors.csv \
    --node=Company=./data/company.csv \
    --node=Conference:Workshop=./data/workshop.csv \
    --node=Conference:RegularConference=./data/conferences_a.csv \
    --node=Conference:ExpertGroup=./data/conferences_b.csv \
    --node=Conference:Symposium=./data/conferences_c.csv \
    --node=Journal=./data/journal.csv \
    --node=Keyword=./data/keywords.csv \
    --node=Review=./data/reviews.csv \
    --node=University=./data/university.csv \
    --node=Venue:Volume=./data/volume.csv \
    --edge=write=Author=Paper=./data/rel_writes.csv \
    --edge=makeReview=Author=Review=./data/rel_gives_review.csv \
    --edge=aboutPaper=Review=Paper=./data/rel_review_about_paper.csv \
    --edge=published=Paper=Venue=./data/rel_published.csv
