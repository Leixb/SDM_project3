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
  in
  rec {
    devShells.default = with pkgs; mkShellNoCC {
      name = "java";
      buildInputs = [
        jdk jdtls # JDTLS requires java > 1.11

        apache-jena
      ];
    };

    packages = {
      inherit jar;
    };

    defaultPackage = packages.jar;
  });
}
