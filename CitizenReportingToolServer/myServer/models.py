from django.db import models
import os
from django.conf import settings
from django.utils.safestring import mark_safe
from django.utils import timezone

class User(models.Model):
    email = models.EmailField(unique=True)
    password = models.CharField(max_length=128)
    phone = models.CharField(max_length=17, blank=True)

    def __str__(self):
        return self.email


class IncidentReport(models.Model):
    description = models.TextField()
    image = models.ImageField(upload_to='incidentImage', blank = True)
    date = models.DateTimeField(editable=False, default=timezone.now)
    user = models.ForeignKey(User, on_delete=models.CASCADE)
    emergencyLevel = models.IntegerField(blank = True, default = 50)
    latitude = models.DecimalField(max_digits=10, decimal_places=8)
    longitude = models.DecimalField(max_digits=10, decimal_places=8)



    def __str__(self):
        return self.description