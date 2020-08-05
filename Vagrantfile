# -*- mode: ruby -*-
# vi: set ft=ruby :
# Build a VM to serve as an Okapi/Docker server
# Deploy development environment

def frontend_port_mapping(config)
  config.vm.network "forwarded_port", guest: 3000, host: 3000
end

def backend_port_mapping(config)
  # config.vm.network "forwarded_port", guest: 8000, host: 8130
  config.vm.network "forwarded_port", guest: 5432, host: 5433
  config.vm.network "forwarded_port", guest: 9130, host: 9130
end

Vagrant.configure(2) do |config|

  # Install vagrant-disksize to allow resizing the vagrant box disk.
  unless Vagrant.has_plugin?("vagrant-disksize")
    raise  Vagrant::Errors::VagrantError.new, "vagrant-disksize plugin is missing. Please install it using 'vagrant plugin install vagrant-disksize' and rerun 'vagrant up'"
  end
  
  config.disksize.size = "200GB"

  # Give us a little headroom
  # Note that provisioning a Stripes webpack requires more RAM
  config.vm.provider "virtualbox" do |vb|
    vb.memory = 16384
    vb.cpus = 6
  end

  # https://app.vagrantup.com/folio/boxes/snapshot-backend-core
  config.vm.define "snapshot-backend-core", autostart: false do |snapshot_backend_core|
    snapshot_backend_core.vm.box = "folio/snapshot-backend-core"
    backend_port_mapping(snapshot_backend_core)
  end

  # https://app.vagrantup.com/folio/boxes/snapshot-core
  config.vm.define "snapshot-core", autostart: false do |snapshot_core|
    snapshot_core.vm.box = "folio/snapshot-core"
    frontend_port_mapping(snapshot_core)
    backend_port_mapping(snapshot_core)
  end

  # https://app.vagrantup.com/folio/boxes/snapshot
  config.vm.define "snapshot", autostart: false do |snapshot|
    snapshot.vm.box = "folio/snapshot"
    frontend_port_mapping(snapshot)
    backend_port_mapping(snapshot)
  end

  # https://app.vagrantup.com/folio/boxes/testing-backend
  config.vm.define "testing-backend", autostart: false do |testing_backend|
    testing_backend.vm.box = "folio/testing-backend"
    backend_port_mapping(testing_backend)
  end

  # https://app.vagrantup.com/folio/boxes/testing
  config.vm.define "testing", autostart: false do |testing|
    testing.vm.box = "folio/testing"
    frontend_port_mapping(testing)
    backend_port_mapping(testing)
  end

  # https://app.vagrantup.com/folio/boxes/release
  config.vm.define "release", autostart: false do |release|
    release.vm.box = "folio/release"
    frontend_port_mapping(release)
    backend_port_mapping(release)
  end

  if Vagrant::Util::Platform.windows?
    config.vm.synced_folder ".", "/vagrant", disabled: "true"
  end

end
