from myServer.models import IncidentReport, User
from myServer.serializers import IncidentReportSerializer
from rest_framework import generics
from django.http import Http404
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status

from rest_framework.parsers import MultiPartParser
from rest_framework.decorators import parser_classes


class IncidentReportList(generics.ListCreateAPIView):
    queryset = IncidentReport.objects.all()
    serializer_class = IncidentReportSerializer


class IncidentReportDetail(generics.RetrieveUpdateDestroyAPIView):
    queryset = IncidentReport.objects.all()
    serializer_class = IncidentReportSerializer