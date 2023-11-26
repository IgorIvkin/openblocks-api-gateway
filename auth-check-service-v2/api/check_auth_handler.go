package api

import (
	"fmt"
	"net/http"
	"os"

	"golang.org/x/crypto/bcrypt"
	app "openblocks.ru/api-gateway/check-auth-service/application"
	repositories "openblocks.ru/api-gateway/check-auth-service/application/repositories"

	"github.com/gin-gonic/gin"
)

type CheckAuthRequest struct {
	Login    string `form:"login"`
	Password string `form:"password"`
}

func ProcessCheckAuth(application *app.Application, ctx *gin.Context) {
	var status bool = doCheckAuth(application, ctx)
	ctx.JSON(http.StatusOK, gin.H{
		"status": status,
	})
}

func doCheckAuth(application *app.Application, ctx *gin.Context) bool {

	var request CheckAuthRequest
	err := ctx.Bind(&request)
	if err != nil {
		fmt.Fprintf(os.Stderr, "Cannot bind request to process check auth: %v\n", err)
		return false
	}

	userDataRepository := repositories.UserDataRepository{
		Application: application,
	}

	userData, err := userDataRepository.GetByLogin(request.Login)
	if err != nil {
		fmt.Fprintf(os.Stderr, "Cannot get user data by login %s: %v\n", request.Login, err)
		return false
	}

	return userData != nil && checkPasswordHash(request.Password, userData.Password)
}

func checkPasswordHash(password string, hash string) bool {
	err := bcrypt.CompareHashAndPassword([]byte(hash), []byte(password))
	return err == nil
}
