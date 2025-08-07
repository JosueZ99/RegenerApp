from rest_framework import viewsets, status
from rest_framework.decorators import action
from rest_framework.response import Response
from rest_framework.filters import SearchFilter, OrderingFilter
from django_filters.rest_framework import DjangoFilterBackend
from django.shortcuts import get_object_or_404
from decimal import Decimal
import math
from .models import (
    CalculationType, Calculation, PaintCalculation, 
    GypsumCalculation, LEDStripCalculation, CableCalculation
)
from .serializers import (
    CalculationTypeSerializer, CalculationListSerializer,
    CalculationDetailSerializer, PaintCalculationSerializer,
    GypsumCalculationSerializer, LEDStripCalculationSerializer,
    CableCalculationSerializer, PaintCalculationRequestSerializer,
    GypsumCalculationRequestSerializer, LEDStripCalculationRequestSerializer,
    CableCalculationRequestSerializer, AddCalculationToBudgetSerializer,
    CalculationResultSerializer
)
from apps.projects.models import Project
from apps.materials.models import Material

class CalculationTypeViewSet(viewsets.ReadOnlyModelViewSet):
    """
    ViewSet de solo lectura para tipos de calculadoras
    """
    queryset = CalculationType.objects.filter(is_active=True)
    serializer_class = CalculationTypeSerializer
    filter_backends = [SearchFilter, OrderingFilter]
    search_fields = ['name', 'description']
    ordering = ['category', 'name']
    filterset_fields = ['category']

class CalculationViewSet(viewsets.ModelViewSet):
    """
    ViewSet para gestión de cálculos
    """
    queryset = Calculation.objects.all()
    serializer_class = CalculationDetailSerializer
    filter_backends = [SearchFilter, OrderingFilter, DjangoFilterBackend]
    search_fields = ['calculation_type__name', 'material__name']
    ordering_fields = ['created_at', 'estimated_cost']
    ordering = ['-created_at']
    filterset_fields = ['project', 'calculation_type', 'added_to_budget']
    
    def get_serializer_class(self):
        """Retorna serializer según la acción"""
        if self.action == 'list':
            return CalculationListSerializer
        return CalculationDetailSerializer
    
    def get_queryset(self):
        """Personalizar queryset según filtros"""
        queryset = Calculation.objects.all()
        
        # Filtro por proyecto seleccionado
        selected_project_only = self.request.query_params.get('selected_project_only')
        if selected_project_only and selected_project_only.lower() == 'true':
            try:
                selected_project = Project.objects.get(is_selected=True)
                queryset = queryset.filter(project=selected_project)
            except Project.DoesNotExist:
                queryset = queryset.none()
        
        return queryset
    
    @action(detail=False, methods=['post'])
    def calculate_paint(self, request):
        """
        Calculadora de pintura
        POST /api/calculations/calculate_paint/
        """
        serializer = PaintCalculationRequestSerializer(data=request.data)
        if not serializer.is_valid():
            return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
        
        data = serializer.validated_data
        
        # Cálculos de pintura
        area = data['area_to_paint']
        coats = data['number_of_coats']
        coverage = data['coverage_per_liter']
        
        # Área total considerando capas
        total_area = area * coats
        
        # Litros necesarios
        liters_needed = total_area / coverage
        
        # Factor de desperdicio (5% por defecto)
        waste_factor = Decimal('0.05')
        liters_with_waste = liters_needed * (1 + waste_factor)
        
        # Convertir a galones (1 galón = 3.785 litros)
        gallons_needed = liters_with_waste / Decimal('3.785')
        
        # Buscar material sugerido
        material = None
        estimated_cost = None
        
        if data.get('material_id'):
            try:
                material = Material.objects.get(id=data['material_id'])
                if material.reference_price:
                    estimated_cost = liters_with_waste * material.reference_price
            except Material.DoesNotExist:
                pass
        
        # Crear registro de cálculo
        calculation_type = CalculationType.objects.get(code='paint')
        
        calculation = Calculation.objects.create(
            project_id=data['project_id'],
            calculation_type=calculation_type,
            material=material,
            input_data=dict(data),
            calculated_quantity=liters_with_waste,
            unit='litros',
            estimated_cost=estimated_cost,
            detailed_results={
                'total_area': float(total_area),
                'base_liters_needed': float(liters_needed),
                'waste_factor_applied': float(waste_factor),
                'gallons_equivalent': float(gallons_needed)
            }
        )
        
        # Crear detalles específicos de pintura
        paint_details = PaintCalculation.objects.create(
            calculation=calculation,
            area_to_paint=area,
            number_of_coats=coats,
            paint_type=data['paint_type'],
            coverage_per_liter=coverage,
            total_liters_needed=liters_with_waste,
            gallons_needed=gallons_needed
        )
        
        # Respuesta unificada
        return Response({
            'calculation_id': calculation.id,
            'calculation_type': 'paint',
            'calculated_quantity': float(liters_with_waste),
            'unit': 'litros',
            'estimated_cost': float(estimated_cost) if estimated_cost else None,
            'detailed_results': calculation.detailed_results,
            'specific_details': {
                'area_to_paint': float(area),
                'number_of_coats': coats,
                'paint_type': data['paint_type'],
                'coverage_per_liter': float(coverage),
                'gallons_needed': float(gallons_needed)
            },
            'material_suggestions': self._get_material_suggestions('construction', 'pintura')
        })
    
    @action(detail=False, methods=['post'])
    def calculate_gypsum(self, request):
        """
        Calculadora de gypsum
        POST /api/calculations/calculate_gypsum/
        """
        serializer = GypsumCalculationRequestSerializer(data=request.data)
        if not serializer.is_valid():
            return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
        
        data = serializer.validated_data
        
        # Cálculos de gypsum
        area = data['area_to_cover']
        thickness = data['thickness']
        
        # Dimensiones estándar de plancha de gypsum (1.22m x 2.44m = 2.9768 m²)
        sheet_area = Decimal('2.9768')
        
        # Planchas necesarias (con 10% de desperdicio)
        waste_factor = Decimal('0.10')
        sheets_base = math.ceil(area / sheet_area)
        sheets_needed = math.ceil(sheets_base * (1 + waste_factor))
        
        # Metros lineales de perfil (perímetro + estructuras internas)
        # Estimación: perímetro + 1 perfil cada 60cm
        perimeter_estimate = 2 * math.sqrt(area * 2)  # Estimación basada en área
        internal_profiles = area / Decimal('0.6')  # 1 perfil cada 60cm
        linear_meters_profile = perimeter_estimate + float(internal_profiles)
        
        # Buscar material sugerido
        material = None
        estimated_cost = None
        
        if data.get('material_id'):
            try:
                material = Material.objects.get(id=data['material_id'])
                if material.reference_price:
                    estimated_cost = sheets_needed * material.reference_price
            except Material.DoesNotExist:
                pass
        
        # Crear registro de cálculo
        calculation_type = CalculationType.objects.get(code='gypsum')
        
        calculation = Calculation.objects.create(
            project_id=data['project_id'],
            calculation_type=calculation_type,
            material=material,
            input_data=dict(data),
            calculated_quantity=sheets_needed,
            unit='planchas',
            estimated_cost=estimated_cost,
            detailed_results={
                'sheet_area': float(sheet_area),
                'sheets_base': sheets_base,
                'waste_factor_applied': float(waste_factor),
                'linear_meters_profile': linear_meters_profile
            }
        )
        
        # Crear detalles específicos de gypsum
        gypsum_details = GypsumCalculation.objects.create(
            calculation=calculation,
            area_to_cover=area,
            thickness=thickness,
            gypsum_type=data['gypsum_type'],
            sheets_needed=sheets_needed,
            linear_meters_profile=Decimal(str(linear_meters_profile))
        )
        
        return Response({
            'calculation_id': calculation.id,
            'calculation_type': 'gypsum',
            'calculated_quantity': sheets_needed,
            'unit': 'planchas',
            'estimated_cost': float(estimated_cost) if estimated_cost else None,
            'detailed_results': calculation.detailed_results,
            'specific_details': {
                'area_to_cover': float(area),
                'thickness': float(thickness),
                'gypsum_type': data['gypsum_type'],
                'linear_meters_profile': linear_meters_profile
            },
            'material_suggestions': self._get_material_suggestions('construction', 'gypsum')
        })
    
    @action(detail=False, methods=['post'])
    def calculate_led_strip(self, request):
        """
        Calculadora de cintas LED
        POST /api/calculations/calculate_led_strip/
        """
        serializer = LEDStripCalculationRequestSerializer(data=request.data)
        if not serializer.is_valid():
            return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
        
        data = serializer.validated_data
        
        # Cálculos de cinta LED
        length = data['total_length']
        power_per_meter = data['power_per_meter']
        meters_per_roll = data['meters_per_roll']
        
        # Potencia total
        total_power = length * power_per_meter
        
        # Rollos necesarios
        rolls_needed = math.ceil(length / meters_per_roll)
        
        # Drivers necesarios (asumiendo 60W por driver)
        watts_per_driver = 60
        drivers_needed = math.ceil(total_power / watts_per_driver)
        
        # Factor de seguridad para drivers (no cargar al 100%)
        safety_factor = 0.8
        drivers_with_safety = math.ceil(total_power / (watts_per_driver * safety_factor))
        
        # Buscar material sugerido
        material = None
        estimated_cost = None
        
        if data.get('material_id'):
            try:
                material = Material.objects.get(id=data['material_id'])
                if material.reference_price:
                    estimated_cost = rolls_needed * material.reference_price
            except Material.DoesNotExist:
                pass
        
        # Crear registro de cálculo
        calculation_type = CalculationType.objects.get(code='led_strip')
        
        calculation = Calculation.objects.create(
            project_id=data['project_id'],
            calculation_type=calculation_type,
            material=material,
            input_data=dict(data),
            calculated_quantity=length,
            unit='metros',
            estimated_cost=estimated_cost,
            detailed_results={
                'total_power': float(total_power),
                'rolls_needed': rolls_needed,
                'drivers_basic': drivers_needed,
                'drivers_with_safety': drivers_with_safety,
                'safety_factor_applied': safety_factor
            }
        )
        
        # Crear detalles específicos de LED
        led_details = LEDStripCalculation.objects.create(
            calculation=calculation,
            total_length=length,
            power_per_meter=power_per_meter,
            voltage=data['voltage'],
            strip_type=data['strip_type'],
            total_power=total_power,
            drivers_needed=drivers_with_safety,
            meters_per_roll=meters_per_roll,
            rolls_needed=rolls_needed
        )
        
        return Response({
            'calculation_id': calculation.id,
            'calculation_type': 'led_strip',
            'calculated_quantity': float(length),
            'unit': 'metros',
            'estimated_cost': float(estimated_cost) if estimated_cost else None,
            'detailed_results': calculation.detailed_results,
            'specific_details': {
                'total_length': float(length),
                'power_per_meter': float(power_per_meter),
                'voltage': data['voltage'],
                'strip_type': data['strip_type'],
                'total_power': float(total_power),
                'drivers_needed': drivers_with_safety,
                'rolls_needed': rolls_needed
            },
            'material_suggestions': self._get_material_suggestions('lighting', 'led')
        })
    
    @action(detail=False, methods=['post'])
    def calculate_cable(self, request):
        """
        Calculadora de cables
        POST /api/calculations/calculate_cable/
        """
        serializer = CableCalculationRequestSerializer(data=request.data)
        if not serializer.is_valid():
            return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
        
        data = serializer.validated_data
        
        # Cálculos de cable
        length = data['total_length']
        meters_per_roll = data['meters_per_roll']
        
        # Factor de seguridad para cables (10% extra)
        safety_factor = Decimal('0.10')
        length_with_safety = length * (1 + safety_factor)
        
        # Rollos necesarios
        rolls_needed = math.ceil(length_with_safety / meters_per_roll)
        
        # Buscar material sugerido
        material = None
        estimated_cost = None
        
        if data.get('material_id'):
            try:
                material = Material.objects.get(id=data['material_id'])
                if material.reference_price:
                    estimated_cost = rolls_needed * material.reference_price
            except Material.DoesNotExist:
                pass
        
        # Crear registro de cálculo
        calculation_type = CalculationType.objects.get(code='cable')
        
        calculation = Calculation.objects.create(
            project_id=data['project_id'],
            calculation_type=calculation_type,
            material=material,
            input_data=dict(data),
            calculated_quantity=length_with_safety,
            unit='metros',
            estimated_cost=estimated_cost,
            detailed_results={
                'base_length': float(length),
                'safety_factor_applied': float(safety_factor),
                'rolls_needed': rolls_needed,
                'total_meters_purchased': rolls_needed * float(meters_per_roll)
            }
        )
        
        # Crear detalles específicos de cable
        cable_details = CableCalculation.objects.create(
            calculation=calculation,
            total_length=length_with_safety,
            wire_gauge=data['wire_gauge'],
            cable_type=data['cable_type'],
            installation_type=data['installation_type'],
            rolls_needed=rolls_needed,
            meters_per_roll=meters_per_roll
        )
        
        return Response({
            'calculation_id': calculation.id,
            'calculation_type': 'cable',
            'calculated_quantity': float(length_with_safety),
            'unit': 'metros',
            'estimated_cost': float(estimated_cost) if estimated_cost else None,
            'detailed_results': calculation.detailed_results,
            'specific_details': {
                'total_length': float(length_with_safety),
                'wire_gauge': data['wire_gauge'],
                'cable_type': data['cable_type'],
                'installation_type': data['installation_type'],
                'rolls_needed': rolls_needed,
                'meters_per_roll': float(meters_per_roll)
            },
            'material_suggestions': self._get_material_suggestions('electrical', 'cable')
        })
    
    @action(detail=True, methods=['post'])
    def add_to_budget(self, request, pk=None):
        """
        Agregar cálculo al presupuesto inicial
        POST /api/calculations/{id}/add_to_budget/
        """
        calculation = self.get_object()
        
        if calculation.added_to_budget:
            return Response(
                {'error': 'Este cálculo ya fue agregado al presupuesto'},
                status=status.HTTP_400_BAD_REQUEST
            )
        
        serializer = AddCalculationToBudgetSerializer(data=request.data)
        if not serializer.is_valid():
            return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
        
        data = serializer.validated_data
        
        # Crear item en presupuesto
        from apps.budgets.models import BudgetItem
        from apps.budgets.serializers import BudgetItemCreateUpdateSerializer
        
        unit_price = data.get('unit_price_override')
        if not unit_price and calculation.material and calculation.material.reference_price:
            unit_price = calculation.material.reference_price
        elif not unit_price:
            unit_price = calculation.estimated_cost / calculation.calculated_quantity if calculation.estimated_cost else 0
        
        budget_data = {
            'project': calculation.project.id,
            'description': f"{calculation.calculation_type.name} - {calculation.calculated_quantity} {calculation.unit}",
            'category': 'construction' if calculation.calculation_type.category == 'construction' else 'lighting',
            'quantity': calculation.calculated_quantity,
            'unit': calculation.unit,
            'unit_price': unit_price,
            'material': calculation.material.id if calculation.material else None,
            'supplier': data.get('supplier_id'),
            'spaces': data.get('spaces', ''),
            'notes': data.get('notes', f"Generado desde calculadora: {calculation.calculation_type.name}"),
            'created_from_calculation': True
        }
        
        budget_serializer = BudgetItemCreateUpdateSerializer(data=budget_data)
        if budget_serializer.is_valid():
            budget_item = budget_serializer.save()
            
            # Marcar cálculo como agregado
            calculation.added_to_budget = True
            calculation.budget_item_created = budget_item
            calculation.save()
            
            return Response({
                'message': 'Cálculo agregado al presupuesto exitosamente',
                'budget_item_id': budget_item.id,
                'calculation': CalculationDetailSerializer(calculation).data
            })
        
        return Response(budget_serializer.errors, status=status.HTTP_400_BAD_REQUEST)
    
    def _get_material_suggestions(self, category_type, material_type_filter=None):
        """
        Obtener sugerencias de materiales para el tipo de cálculo
        """
        materials = Material.objects.filter(
            category__category_type=category_type,
            is_active=True
        )
        
        if material_type_filter:
            materials = materials.filter(name__icontains=material_type_filter)
        
        from apps.materials.serializers import MaterialForCalculatorSerializer
        return MaterialForCalculatorSerializer(materials[:5], many=True).data