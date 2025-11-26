package eu.luminis.passkeystryout.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "passkey_credentials")
public class PasskeyCredential {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 1024)
    private String credentialId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String publicKey;
    
    @Column(nullable = false)
    private long signCount;
    
    @Column(nullable = false, length = 512)
    private String label;
    
    @Column(nullable = false)
    private Instant created;
    
    @Column(nullable = false)
    private Instant lastUsed;
    
    @Column(length = 1024)
    private String transports;
    
    @Column(nullable = false)
    private boolean backupEligible;
    
    @Column(nullable = false)
    private boolean backupState;
    
    @Column(length = 512)
    private String attestationObject;
    
    @Column(length = 512)
    private String clientDataJSON;
    
    @Column(length = 255)
    private String authenticatorAttachment;
    
    public PasskeyCredential() {}
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getCredentialId() {
        return credentialId;
    }
    
    public void setCredentialId(String credentialId) {
        this.credentialId = credentialId;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public String getPublicKey() {
        return publicKey;
    }
    
    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }
    
    public long getSignCount() {
        return signCount;
    }
    
    public void setSignCount(long signCount) {
        this.signCount = signCount;
    }
    
    public String getLabel() {
        return label;
    }
    
    public void setLabel(String label) {
        this.label = label;
    }
    
    public Instant getCreated() {
        return created;
    }
    
    public void setCreated(Instant created) {
        this.created = created;
    }
    
    public Instant getLastUsed() {
        return lastUsed;
    }
    
    public void setLastUsed(Instant lastUsed) {
        this.lastUsed = lastUsed;
    }
    
    public String getTransports() {
        return transports;
    }
    
    public void setTransports(String transports) {
        this.transports = transports;
    }
    
    public boolean isBackupEligible() {
        return backupEligible;
    }
    
    public void setBackupEligible(boolean backupEligible) {
        this.backupEligible = backupEligible;
    }
    
    public boolean isBackupState() {
        return backupState;
    }
    
    public void setBackupState(boolean backupState) {
        this.backupState = backupState;
    }
    
    public String getAttestationObject() {
        return attestationObject;
    }
    
    public void setAttestationObject(String attestationObject) {
        this.attestationObject = attestationObject;
    }
    
    public String getClientDataJSON() {
        return clientDataJSON;
    }
    
    public void setClientDataJSON(String clientDataJSON) {
        this.clientDataJSON = clientDataJSON;
    }
    
    public String getAuthenticatorAttachment() {
        return authenticatorAttachment;
    }
    
    public void setAuthenticatorAttachment(String authenticatorAttachment) {
        this.authenticatorAttachment = authenticatorAttachment;
    }
}
