# -*- mode: ruby -*-
# vi: set ft=ruby :
# Build a VM to serve as an Okapi/Docker server
# Deploy development environment

Vagrant.configure(2) do |config|

  if Vagrant::Util::Platform.windows?
    config.vm.synced_folder ".", "/vagrant", disabled: "true"
  end

  config.vm.provider "virtualbox" do |vb|
    vb.memory = 12288
    vb.cpus = 4
  end

  # https://app.vagrantup.com/folio/boxes/snapshot-backend-core
  config.vm.define "snapshot-backend-core", autostart: false do |snapshot_backend_core|
    snapshot_backend_core.vm.box = "folio/snapshot-backend-core"
  end

  # https://app.vagrantup.com/folio/boxes/snapshot-core
  config.vm.define "snapshot-core", autostart: false do |snapshot_core|
    snapshot_core.vm.box = "folio/snapshot-core"
  end

  # https://app.vagrantup.com/folio/boxes/snapshot
  config.vm.define "snapshot", autostart: false do |snapshot|
    snapshot.vm.box = "folio/snapshot"
  end

  # https://app.vagrantup.com/folio/boxes/testing-backend
  config.vm.define "testing-backend", autostart: false do |testing_backend|
    testing_backend.vm.box = "folio/testing-backend"
  end

  # https://app.vagrantup.com/folio/boxes/testing
  config.vm.define "testing", autostart: false do |testing|
    testing.vm.box = "folio/testing"
  end

  # https://app.vagrantup.com/folio/boxes/release
  config.vm.define "release", autostart: false do |release|
    release.vm.box = "folio/release"
    release.vm.network "forwarded_port", guest: 5432, host: 5433
    release.vm.box_version = "1.0.0-20201216.5501"
  end

end
