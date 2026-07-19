CREATE TABLE importacao (
    id UUID PRIMARY KEY,
    protocolo VARCHAR(255) NOT NULL,
    caminho_arquivo VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL,
    total_registros INTEGER NOT NULL,
    sucesso INTEGER NOT NULL,
    falhas INTEGER NOT NULL,
    CONSTRAINT uk_importacao_protocolo UNIQUE (protocolo)
);

CREATE TABLE importacao_erro (
    id UUID PRIMARY KEY,
    importacao_id UUID NOT NULL,
    linha INTEGER NOT NULL,
    mensagem VARCHAR(255) NOT NULL,
    CONSTRAINT fk_importacao_erro_importacao
        FOREIGN KEY (importacao_id) REFERENCES importacao (id)
        ON DELETE CASCADE
);

CREATE INDEX idx_importacao_erro_importacao_id ON importacao_erro (importacao_id);
