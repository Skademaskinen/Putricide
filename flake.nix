{
  description = "Java Discord bot system modules";
  inputs = {
    nixpkgs.url = "nixpkgs/nixos-24.05";
  };
  outputs = { self, nixpkgs }: let
    system = "x86_64-linux";
    pkgs = import nixpkgs {inherit system;};
  in rec {
    nixosModules.default = {pkgs, config, lib, ...}:

    {
      options.services.putricide = {
        path = lib.mkOption {
          type = lib.types.str;
          default = "/var/run/putricide";
        };
        enable = lib.mkOption {
          type = lib.types.bool;
          default = false;
        };
        args = lib.mkOption {
          type = lib.types.listOf lib.types.str;
          default = [];
        };
      };
      config.systemd.services.putricide = {
        enable = config.services.putricide.enable;
        description = "Java Discord bot systemd service";
        serviceConfig = {
          ExecStart = "${packages.${system}.default}/bin/putricide-wrapped --config ${config.services.putricide.path} --source ${packages.${system}.source}/share/putricide ${lib.concatStrings (lib.strings.intersperse "" config.services.putricide.args)}";
        };
        wantedBy = ["default.target"];
        after = ["network-online.target"];
        wants = ["network-online.target"];
      };
    };
    packages.${system} = {
      source = pkgs.maven.buildMavenPackage rec {
        pname = "ppbot";
        name = "putricide";
        version = "3.38a";

        mvnParameters = "-f ${pname}";
        mvnHash = "sha256-cuJvC/yYEC9ok2991y0VjGhycNBnaDOPv1SxZj6lrjA=";

        src = ./.;

        installPhase = ''
          mkdir -p $out/{lib,share}/putricide
          cp ${pname}-${version}.jar $out/lib/putricide/putricide.jar
          cp -r $src/* $out/share/putricide
        '';
      };
      default = pkgs.writeScriptBin "putricide-wrapped" ''
        #!${pkgs.bash}/bin/bash

        ${pkgs.jdk21}/bin/java -jar ${packages.${system}.source}/lib/putricide/putricide.jar $@
      '';
    };

  };
}
