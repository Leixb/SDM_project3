SDM Lab assignment 3
====================

[![built with nix](https://img.shields.io/static/v1?logo=nixos&logoColor=white&label=&message=Built%20with%20Nix&color=41439a)](https://builtwithnix.org "built with nix")
[![Build RDFS with nix](https://github.com/Leixb/SDM_project3/actions/workflows/build_rdfs.yaml/badge.svg)](https://github.com/Leixb/SDM_project3/actions/workflows/build_rdfs.yaml)

Hands-on lab on knowledge graphs.

This repo provides the data and code to generate the RDFS files required in the
assignment. The data is obtained from [Aminer](https://www.aminer.org/citation)
using our [json parser from lab 1](https://github.com/Leixb/Aminer-citations-to-csv-for-neo4j/tree/sdm-lab3).
The CSV files where modified to suit our needs and we only took the data from
the first 10k entries instead of the full 5 million from the dataset reduce the
processing time.

The file `./process_all.sh` contains an example of the program usage. Basically
you have to provide the name of the nodes as well as the files separated by `=` (similarly to how
`neo4j-admin import` works).
