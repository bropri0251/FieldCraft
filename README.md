# FieldCraft

**Demo video (~5 min):** <ADD PUBLIC LINK HERE>

FieldCraft is a mobile knowledge hub for airsoft techs and tinkerers.

## Features
- Registration/Login → Main → Summary/Search (3 connected Activities)
- Search & filter across categories (Tech Corner, Maintenance, Safety, Electronics)
- Create/Contribute entries (biometric-gated)
- Offline seed content (≥12 records)
- Polished UI with accessible, high-contrast theme

## Security (Week 5)
- EncryptedSharedPreferences for session data
- Login lockout with exponential backoff
- Biometric/Device Credential for sensitive actions
- No cleartext traffic (Network Security Config)
- Release build with code shrinking/obfuscation
- Input validation and parameterized queries

See [`SECURITY.md`](./SECURITY.md) for details.
