from django.db import models

# Create your models here.

class User2FA(models.Model):
    email = models.EmailField(unique=True)
    secret = models.CharField(max_length=32)
    enabled = models.BooleanField(default=False)

    def __str__(self):
        return self.email
