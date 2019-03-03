from django.urls import path
from myServer import views

urlpatterns = [
    path('myServer/', views.IncidentReportList.as_view()),
    path('myServer/<int:pk>/', views.IncidentReportDetail.as_view()),
]