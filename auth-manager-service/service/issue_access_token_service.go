package service

import (
	"fmt"
	"os"
	"time"

	"github.com/golang-jwt/jwt/v5"
	"github.com/google/uuid"
	app "openblocks.ru/api-gateway/auth-manager-service/application"
)

type AccessTokenClaims struct {
	Guid string `json:"guid"`
	jwt.RegisteredClaims
}

// Issues a new access token based on private key defined in config.yml.
func IssueAccessToken(application *app.Application, expiresAt time.Time) (string, error) {

	privateKey, err := jwt.ParseECPrivateKeyFromPEM([]byte(application.Config.Signing.PrivateKey))
	if err != nil {
		fmt.Fprintf(os.Stderr, "Cannot parse private key: %v\n", err)
		return "", err
	}

	newUuid := uuid.NewString()

	claims := AccessTokenClaims{
		newUuid,
		jwt.RegisteredClaims{
			ExpiresAt: jwt.NewNumericDate(expiresAt),
			Issuer:    "openblocks-auth-manager-service",
		},
	}
	token := jwt.NewWithClaims(jwt.SigningMethodES512, claims)

	signedAccessToken, err := token.SignedString(privateKey)
	if err != nil {
		fmt.Fprintf(os.Stderr, "Cannot sign access token: %v\n", err)
		return "", err
	}

	return signedAccessToken, nil
}
