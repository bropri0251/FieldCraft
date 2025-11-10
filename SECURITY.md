
# Security Controls

- **Storage**: EncryptedSharedPreferences for auth/session
- **Auth Hardening**: Lockout/backoff; persisted failures
- **Step-Up Auth**: Biometric/device credential for write ops
- **Transport**: Cleartext disabled via Network Security Config
- **Build**: R8/proguard + shrink; debug logs disabled in release
- **Validation**: Length limits, allowed chars, parameterized SQL
- **Permissions**: Least privilege
