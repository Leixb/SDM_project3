#!/usr/bin/env bash

NIX="nix run .#jar --"
RUNNER="${RUNNER:-$NIX}"
OUTPUT="${OUTPUT:-papers.rdf}"
DATA="${DATA:-./data}"

$RUNNER \
    --output="$OUTPUT" \
    --node=Paper:Poster="${DATA}/papers_a.csv" \
    --node=Paper:DemoPaper="${DATA}/papers_b.csv" \
    --node=Paper:FullPaper="${DATA}/papers_c.csv" \
    --node=Paper:ShortPaper="${DATA}/papers_d.csv" \
    --node=Author="${DATA}/authors.csv" \
    --node=Conference:Workshop="${DATA}/workshop.csv" \
    --node=Conference:RegularConference="${DATA}/conferences_a.csv" \
    --node=Conference:ExpertGroup="${DATA}/conferences_b.csv" \
    --node=Conference:Symposium="${DATA}/conferences_c.csv" \
    --node=Journal="${DATA}/journal.csv" \
    --node=Keyword="${DATA}/keywords.csv" \
    --node=Review="${DATA}/reviews.csv" \
    --node=PublicationMedium:JournalVolume="${DATA}/volume.csv" \
    --node=PublicationMedium:ConferenceProceeding="${DATA}/edition.csv" \
    --node=Area="${DATA}/area.csv" \
    --edge=paperRelatedTo=Paper=Area="${DATA}/rel_related.csv" \
    --edge=write=Author=Paper="${DATA}/rel_writes.csv" \
    --edge=makeReview=Author=Review="${DATA}/rel_gives_review.csv" \
    --edge=aboutPaper=Review=Paper="${DATA}/rel_review_about_paper.csv" \
    --edge=includedIn=Paper=PublicationMedium="${DATA}/rel_published.csv"
