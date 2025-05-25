package id.ac.ui.cs.advprog.kilimanjaro.authentication;

import java.security.Key;

public interface SigningKeyProvider {
    Key getKey();
}
