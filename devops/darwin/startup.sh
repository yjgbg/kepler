colima start --kubernetes --runtime containerd --cpu 8 --memory 32
operator-sdk olm install
flux install
flux create source git kepler --url=https://github.com/yjgbg/kepler.git --branch main --interval=10s
flux create kustomization infra --source=kepler --path=./devops/darwin/infra/entrypoint --prune=true --interval=10s