{
  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixpkgs-unstable";

    flake-utils = {
      url = "github:numtide/flake-utils";
      inputs.nixpkgs.follows = "nixpkgs";
    };

  };
  outputs = { self, nixpkgs, flake-utils, ...}:

  flake-utils.lib.eachDefaultSystem (system:
  let
    pkgs = import nixpkgs { inherit system; };
    jar = pkgs.callPackage ./.jar.nix { };
    jdtls = pkgs.callPackage ./.jdtls.nix { };
    protege = pkgs.writeShellScriptBin "protege" ''
      _JAVA_OPTIONS="-Dawt.useSystemAAFontSettings=lcd" ${pkgs.protege-distribution}/bin/run-protege
    '';
  in
  rec {
    devShells.default = with pkgs; mkShellNoCC {
      name = "java jena protege";

      buildInputs = [
        jdk jdtls # JDTLS requires java > 1.11

        apache-jena
        protege
      ];
    };

    packages = {
      inherit jar;

      rdfs = pkgs.runCommand "sdmlab3.rdf" {} ''
        export RUNNER="${self.packages.${system}.jar}/bin/sdm-lab"
        export OUTPUT="$out"
        export DATA="${./data}"

        ${builtins.readFile ./process_all.sh}
      '';
    };

    defaultPackage = packages.jar;
  });
}
