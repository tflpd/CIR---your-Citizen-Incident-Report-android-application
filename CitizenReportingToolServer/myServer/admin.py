from django.contrib import admin
from .models import IncidentReport, User
import django.contrib.auth.admin
import django.contrib.auth.models
from django.contrib import auth

class IncidentReportAdmin(admin.ModelAdmin):
	search_fields = ['description']
	fields = ['description','date', 'image', 'user', 'emergencyLevel', 'latitude', 'longitude']
	readonly_fields = ('date',)
	list_display = ('description', 'date','image', 'user', 'emergencyLevel',)
	list_filter = ['date',]

class UserAdmin(admin.ModelAdmin):
	fields = ['email', 'password',]
	list_display = ('id', 'email', 'password',)

admin.site.register(IncidentReport,IncidentReportAdmin)
admin.site.register(User, UserAdmin)

admin.site.unregister(auth.models.User)
admin.site.unregister(auth.models.Group)