# Known Issues - Local Overlay

## ConfigMap Issue (As of 2026-01-02)

### Problem

When running `kubectl kustomize k8s/overlays/local`, the following error occurs:

```
error: merging from generator: id resid.ResId{...Kind:"ConfigMap", Name:"eureka-server-config"...}
does not exist; cannot merge or replace
```

### Root Cause

The local overlay references a ConfigMap named `eureka-server-config` that:
- Does not exist in the base manifests
- Was likely removed or renamed in a previous refactoring
- Is being referenced in the local overlay's kustomization.yaml

### Impact

- **Dev overlay**: ✅ Works correctly (validated 2026-01-02)
- **Prod overlay**: ✅ Works correctly (validated 2026-01-02)
- **Local overlay**: ❌ Fails during kustomize build

### Workaround

Use the dev or prod overlays for EKS deployments. The local overlay is primarily for Minikube
development, which is less frequently used.

### Resolution Required

To fix this issue:

1. Review `k8s/overlays/local/kustomization.yaml`
2. Identify references to `eureka-server-config` ConfigMap
3. Either:
   - Create the missing ConfigMap in base or local overlay
   - Remove the reference if no longer needed
   - Update to use the correct ConfigMap name (e.g., `oficina-shared-config`)

### Related Files

- `k8s/overlays/local/kustomization.yaml`
- `k8s/base/eureka-configmap.yaml` (if it exists)
- `k8s/base/configmap-shared.yaml`

---

**Status**: Known issue, low priority (dev/prod overlays unaffected)
**Reported**: 2026-01-02
**Reporter**: Health probe improvement work
