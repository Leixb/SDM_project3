#!/usr/bin/env bash

OUTPUT="${OUTPUT:-papers.rdf}"
DATA="${DATA:-./data}"

# Note: DATA and OUTPUT may break if there are spaces in the path

ARGUMENTS=(
    "--output=$OUTPUT"
    "--node=Paper:Poster=${DATA}/papers_a.csv"
    "--node=Paper:DemoPaper=${DATA}/papers_b.csv"
    "--node=Paper:FullPaper=${DATA}/papers_c.csv"
    "--node=Paper:ShortPaper=${DATA}/papers_d.csv"
    "--node=Author=${DATA}/authors.csv"
    "--node=Conference:Workshop=${DATA}/workshop.csv"
    "--node=Conference:RegularConference=${DATA}/conferences_a.csv"
    "--node=Conference:ExpertGroup=${DATA}/conferences_b.csv"
    "--node=Conference:Symposium=${DATA}/conferences_c.csv"
    "--node=Journal=${DATA}/journal.csv"
    "--node=Keyword=${DATA}/keywords.csv"
    "--node=Review=${DATA}/reviews.csv"
    "--node=PublicationMedium:JournalVolume=${DATA}/volume.csv"
    "--node=PublicationMedium:ConferenceProceeding=${DATA}/edition.csv"
    "--node=Area=${DATA}/area.csv"
    "--edge=paperRelatedTo=Paper=Area=${DATA}/rel_related.csv"
    "--edge=write=Author=Paper=${DATA}/rel_writes.csv"
    "--edge=makeReview=Author=Review=${DATA}/rel_gives_review.csv"
    "--edge=aboutPaper=Review=Paper=${DATA}/rel_review_about_paper.csv"
    "--edge=includedIn=Paper=PublicationMedium=${DATA}/rel_published.csv"
    "--edge=venueRelatedTo=Venue=Area=${DATA}/rel_venue_related.csv"
    "--edge=submittedTo=Paper=Venue=${DATA}/rel_submittedTo.csv"
)

# Add command line arguments if any
ARGUMENTS+=( "$@" )

run_maven() {
    # mvn exec:java -Dexec.args=\""${ARGUMENTS[@]}"\"
    # The method above does not work, using the pipe does...
    echo mvn exec:java -Dexec.args=\""${ARGUMENTS[@]}"\" | bash
}

run_nix() {
    nix run '.#jar' -- "${ARGUMENTS[@]}"
}

# if RUNNER is set run it, otherwise try to guess
if [ ! -z "$RUNNER" ]; then
    "$RUNNER" "${ARGUMENTS[@]}"
else
    # If nix is available, use it, otherwise use maven
    command -v nix 2> /dev/null && run_nix || run_maven
fi
